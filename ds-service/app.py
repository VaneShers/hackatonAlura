import os
import math
from typing import Tuple, List, Optional

from flask import Flask, request, jsonify

try:
    import joblib  # type: ignore
except Exception:
    joblib = None

app = Flask(__name__)


# Heuristic fallback model (canonical fields)
def heuristic_score(features: dict) -> Tuple[str, float, List[str]]:
    tenure = float(features.get("tenure", 0) or 0)
    monthly = float(features.get("MonthlyCharges", 0.0) or 0.0)
    total = float(features.get("TotalCharges", 0.0) or 0.0)
    senior = int(features.get("SeniorCitizen", 0) or 0)
    contract = str(features.get("Contract", "") or "")
    online_security = str(features.get("OnlineSecurity", "") or "")

    c_tenure = -0.03 * tenure
    c_monthly = -0.01 * monthly
    c_total = -0.005 * total
    c_senior = 0.1 if senior == 1 else 0.0
    c_contract = 0.15 if contract == "Month-to-month" else (-0.05 if contract == "Two year" else 0.0)
    c_security = 0.08 if online_security == "No" else 0.0

    z = -1.0 + c_tenure + c_monthly + c_total + c_senior + c_contract + c_security
    p = 1.0 / (1.0 + math.exp(-z))
    label = "Va a cancelar" if p >= 0.5 else "Va a continuar"

    contrib = {
        "tenure": abs(c_tenure),
        "Contract": abs(c_contract),
        "OnlineSecurity": abs(c_security),
    }
    top = sorted(contrib, key=lambda k: contrib[k], reverse=True)[:3]
    return label, p, top


# Optional trained model loading
MODEL = None
FEATURE_NAMES: Optional[List[str]] = None


# plan no longer used


def _to_vector(features: dict, names: List[str]) -> List[float]:
    # Map incoming canonical JSON features to vector if pipeline expects numeric order
    vec = []
    for n in names:
        v = features.get(n)
        try:
            vec.append(float(v if v is not None and v != "" else 0))
        except Exception:
            vec.append(0.0)
    return vec


def load_model():
    global MODEL, FEATURE_NAMES
    model_dir = os.getenv("CHURN_MODEL_DIR", "/models")
    pipeline_path = os.path.join(model_dir, "churn_pipeline.pkl")
    features_path = os.path.join(model_dir, "feature_names.pkl")
    if not joblib:
        return
    try:
        if os.path.exists(pipeline_path):
            MODEL = joblib.load(pipeline_path)
        if os.path.exists(features_path):
            FEATURE_NAMES = joblib.load(features_path)
    except Exception as e:
        # Keep fallback if loading fails
        MODEL = None
        FEATURE_NAMES = None


load_model()


def predict_with_model(features: dict) -> Optional[Tuple[str, float, List[str]]]:
    if MODEL is None or FEATURE_NAMES is None:
        return None
    try:
        vec = _to_vector(features, FEATURE_NAMES)
        import numpy as np  # local import to avoid hard dependency on startup
        X = np.array(vec, dtype=float).reshape(1, -1)
        if hasattr(MODEL, "predict_proba"):
            proba = MODEL.predict_proba(X)[0]
            p1 = float(proba[1]) if len(proba) > 1 else float(proba[0])
        else:
            # Fallback to decision_function or predict as probability proxy
            if hasattr(MODEL, "decision_function"):
                z = float(MODEL.decision_function(X)[0])
                p1 = 1.0 / (1.0 + math.exp(-z))
            else:
                pred = MODEL.predict(X)[0]
                p1 = 0.8 if int(pred) == 1 else 0.2
        label = "Va a cancelar" if p1 >= 0.5 else "Va a continuar"

        # Approximate feature contributions if coef_ exists
        top = FEATURE_NAMES[:]
        try:
            lr = getattr(MODEL, "named_steps", {}).get("logisticregression") or MODEL
            coef = getattr(lr, "coef_", None)
            if coef is not None:
                w = coef[0]
                contrib = {FEATURE_NAMES[i]: abs(w[i] * vec[i]) for i in range(len(FEATURE_NAMES))}
                top = sorted(contrib, key=lambda k: contrib[k], reverse=True)[:3]
        except Exception:
            top = FEATURE_NAMES[:3]

        return label, p1, top
    except Exception:
        return None


@app.route("/predict", methods=["POST"])
def predict():
    payload = request.get_json(silent=True) or {}
    feats = payload.get("features") or payload
    # Enforce input rules: case-sensitive strings, TotalCharges null->0.0
    if "TotalCharges" in feats and (feats["TotalCharges"] is None or feats["TotalCharges"] == ""):
        feats["TotalCharges"] = 0.0

    # Try model first, fallback to heuristic
    out = predict_with_model(feats)
    if out is None:
        out = heuristic_score(feats)
    label, prob, top = out

    # Enriched response
    risk = "Alto Riesgo" if prob >= 0.66 else ("Riesgo Medio" if prob >= 0.33 else "Bajo Riesgo")
    will = 1 if prob >= 0.5 else 0
    conf = max(0.5, abs(prob - 0.5) * 2)
    action = "Retención Prioritaria / Oferta de Lealtad" if will == 1 else "Upsell / Programa de Fidelización"

    return jsonify({
        "metadata": {"model_version": "v1.0", "timestamp": os.getenv("MODEL_TIMESTAMP", "")},
        "prediction": {
            "churn_probability": prob,
            "will_churn": will,
            "risk_level": risk,
            "confidence_score": conf
        },
        "business_logic": {"suggested_action": action},
        # Legacy keys for backward compatibility
        "prevision": label,
        "probabilidad": prob,
        "top_features": top
    })


@app.route("/")
def home():
    status = {
        "service": "ds",
        "modelLoaded": MODEL is not None and FEATURE_NAMES is not None
    }
    return jsonify(status)


@app.route("/health")
def health():
    return jsonify({"status": "UP"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
