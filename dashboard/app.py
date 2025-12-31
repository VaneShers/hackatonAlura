import os
import io
import json
import requests
import pandas as pd
import streamlit as st


st.set_page_config(page_title="Churn Alert Dashboard", page_icon="📊", layout="wide")
st.title("Churn Alert Dashboard")
st.caption("Interactúa con la API para predecir churn, procesar CSV por lotes y ver estadísticas.")


def get_api_base_url():
    default_url = os.getenv("CHURN_API_URL", "http://localhost:8080")
    return st.sidebar.text_input("API base URL", value=default_url, help="Ej: http://localhost:8080")


def get_auth_token():
    return st.sidebar.text_input(
        "Bearer token (opcional)",
        type="password",
        help="Pega solo el valor del token o con el prefijo 'Bearer '. Ambos funcionan.",
    )


def _normalize_token(token: str | None) -> str | None:
    if not token:
        return None
    t = token.strip()
    if t.lower().startswith("bearer "):
        t = t.split(" ", 1)[1].strip()
    return t


def build_headers(token: str | None):
    headers = {"Content-Type": "application/json"}
    norm = _normalize_token(token)
    if norm:
        headers["Authorization"] = f"Bearer {norm}"
    return headers


def call_predict(api_url: str, payload: dict, token: str | None):
    try:
        resp = requests.post(f"{api_url}/api/churn/predict", headers=build_headers(token), data=json.dumps(payload), timeout=15)
        return resp
    except requests.RequestException as e:
        st.error(f"Error de red al llamar predict: {e}")
        return None


def call_stats(api_url: str, token: str | None):
    try:
        resp = requests.get(f"{api_url}/api/churn/stats", headers=build_headers(token), timeout=10)
        return resp
    except requests.RequestException as e:
        st.error(f"Error de red al llamar stats: {e}")
        return None


def call_batch_csv(api_url: str, csv_bytes: bytes, filename: str, token: str | None):
    try:
        headers = {}
        if token:
            headers["Authorization"] = f"Bearer {token}"
        files = {"file": (filename, io.BytesIO(csv_bytes), "text/csv")}
        resp = requests.post(f"{api_url}/api/churn/predict/batch/csv", headers=headers, files=files, timeout=30)
        return resp
    except requests.RequestException as e:
        st.error(f"Error de red al llamar batch CSV: {e}")
        return None


api_url = get_api_base_url()
token = get_auth_token()

tab_individual, tab_batch, tab_stats = st.tabs(["Predicción individual", "Batch CSV", "Estadísticas"])

with tab_individual:
    st.subheader("Predicción individual")
    with st.form("form_individual"):
        col1, col2 = st.columns(2)
        with col1:
            tiempo_contrato_meses = st.number_input("Tiempo de contrato (meses)", min_value=0, step=1, value=12)
            retrasos_pago = st.number_input("Retrasos de pago", min_value=0, step=1, value=2)
        with col2:
            uso_mensual = st.number_input("Uso mensual", min_value=0.0, step=0.1, value=14.5)
            plan = st.selectbox("Plan", options=["Basic", "Standard", "Premium"], index=2)

        submitted = st.form_submit_button("Predecir")

    if submitted:
        payload = {
            "tiempo_contrato_meses": int(tiempo_contrato_meses),
            "retrasos_pago": int(retrasos_pago),
            "uso_mensual": float(uso_mensual),
            "plan": plan,
        }

        resp = call_predict(api_url, payload, token)
        if resp is None:
            st.stop()

        if resp.status_code == 200:
            data = resp.json()
            prevision = data.get("prevision")
            prob = data.get("probabilidad")
            top_feats = data.get("topFeatures") or data.get("top_features")

            st.success("Predicción recibida")
            colA, colB = st.columns(2)
            with colA:
                st.metric("Previsión", prevision)
                if isinstance(prob, (int, float)):
                    st.progress(min(max(prob, 0), 1))
                    st.caption(f"Probabilidad: {prob:.2f}")
            with colB:
                if top_feats:
                    st.write("Top features:", top_feats)
            st.code(json.dumps(data, ensure_ascii=False, indent=2), language="json")
        elif resp.status_code == 400:
            try:
                err = resp.json()
                st.error("Error de validación")
                st.code(json.dumps(err, ensure_ascii=False, indent=2), language="json")
            except Exception:
                st.error(f"Solicitud inválida: {resp.text}")
        else:
            st.error(f"Error {resp.status_code}: {resp.text}")


with tab_batch:
    st.subheader("Predicción por lotes (CSV)")
    st.caption("Encabezados requeridos: tiempo_contrato_meses,retrasos_pago,uso_mensual,plan")
    uploaded = st.file_uploader("Subir CSV", type=["csv"])

    if uploaded is not None:
        # Vista previa local
        try:
            df_preview = pd.read_csv(uploaded)
            st.dataframe(df_preview.head(20), use_container_width=True)
        except Exception as e:
            st.warning(f"No se pudo leer el CSV para vista previa: {e}")

        # Enviar al backend
        resp = call_batch_csv(api_url, uploaded.getvalue(), uploaded.name, token)
        if resp is None:
            st.stop()

        if resp.status_code == 200:
            data = resp.json()
            items = data.get("items", [])
            total = data.get("total")
            cancelaciones = data.get("cancelaciones")
            st.success("Resultados del lote")
            st.write(f"Total: {total} | Cancelaciones: {cancelaciones}")
            try:
                st.dataframe(pd.DataFrame(items), use_container_width=True)
            except Exception:
                st.write(items)
            st.code(json.dumps(data, ensure_ascii=False, indent=2), language="json")
        elif resp.status_code == 400:
            try:
                err = resp.json()
                st.error("Error al procesar CSV")
                st.code(json.dumps(err, ensure_ascii=False, indent=2), language="json")
            except Exception:
                st.error(f"Solicitud inválida: {resp.text}")
        else:
            st.error(f"Error {resp.status_code}: {resp.text}")


with tab_stats:
    st.subheader("Estadísticas")
    resp = call_stats(api_url, token)
    if resp:
        if resp.status_code == 200:
            stats = resp.json()
            st.json(stats)
        else:
            st.warning(f"No disponible ({resp.status_code})")
