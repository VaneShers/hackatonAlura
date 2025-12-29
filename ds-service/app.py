from flask import Flask, request, jsonify

app = Flask(__name__)

# Simple heuristic model mirroring Java fallback, but returning top_features too

def score(features):
    plan = (features.get("plan") or "").lower()
    plan_risk = {"basic": 0.15, "standard": 0.10, "premium": 0.05}.get(plan, 0.12)
    retrasos = float(features.get("retrasos_pago", 0))
    tiempo = float(features.get("tiempo_contrato_meses", 0))
    uso = float(features.get("uso_mensual", 0))

    c_retrasos = 0.08 * retrasos
    c_tiempo = -0.03 * tiempo
    c_uso = -0.02 * uso
    c_plan = plan_risk

    z = -1.0 + c_retrasos + c_tiempo + c_uso + c_plan
    import math
    p = 1.0 / (1.0 + math.exp(-z))
    label = "Va a cancelar" if p >= 0.5 else "Va a continuar"

    contrib = {
        "retrasos_pago": abs(c_retrasos),
        "tiempo_contrato_meses": abs(c_tiempo),
        "uso_mensual": abs(c_uso),
        "plan": abs(c_plan),
    }
    top = sorted(contrib, key=lambda k: contrib[k], reverse=True)[:3]
    return label, p, top

@app.route("/predict", methods=["POST"])
def predict():
    payload = request.get_json(silent=True) or {}
    feats = payload.get("features") or {}
    label, prob, top = score(feats)
    return jsonify({
        "prevision": label,
        "probabilidad": prob,
        "top_features": top
    })

@app.route("/")
def home():
    return jsonify({"message": "DS microservice for ChurnInsight is running"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
