from flask import Flask, request, jsonify
import joblib
import os
import pandas as pd

app = Flask(__name__)


modelo = joblib.load("churn_model_pipeline.joblib")

FEATURES = [
    'gender', 'SeniorCitizen', 'Partner', 'Dependents', 'tenure',
    'PhoneService', 'MultipleLines', 'InternetService', 'OnlineSecurity',
    'OnlineBackup', 'DeviceProtection', 'TechSupport', 'StreamingTV',
    'StreamingMovies', 'Contract', 'PaperlessBilling', 'PaymentMethod',
    'MonthlyCharges', 'TotalCharges','IsNewCustomer','AvgMonthlyCharge'
]



@app.route("/predict", methods=["POST"])
def predict():
    try:
        
        data = request.get_json()

        df = pd.DataFrame([{f: data[f] for f in FEATURES}])

        # Hacer predicción
        pred = modelo.predict(df)[0]
        proba = modelo.predict_proba(df)[0][1]

        prevision = "Va a cancelar" if pred == 1 else "No va a cancelar"

        return jsonify({
            "prevision": prevision,
            "probabilidad": round(float(proba), 2)
        })

    except Exception as e:
        return jsonify({
            "error": str(e)
        }), 400

if __name__ == "__main__":
    app.run(debug=True)