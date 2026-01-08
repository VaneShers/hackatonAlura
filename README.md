# Churn Alert

## Objetivo
API REST que recibe datos de cliente y devuelve predicción de churn y probabilidad. Integra con un microservicio de Data Science (DS).

## Endpoints
- POST `/api/churn/predict` (JSON)
  - Protegido: requiere `Authorization: Bearer <token>`
  - Entrada (20 variables canónicas, sin `customerID` ni `Churn`):
    ```json
    {
      "gender": "Female",
      "SeniorCitizen": 0,
      "Partner": "Yes",
      "Dependents": "No",
      "tenure": 24,
      "PhoneService": "Yes",
      "MultipleLines": "No",
      "InternetService": "DSL",
      "OnlineSecurity": "Yes",
      "OnlineBackup": "No",
      "DeviceProtection": "No",
      "TechSupport": "No",
      "StreamingTV": "No",
      "StreamingMovies": "No",
      "Contract": "One year",
      "PaperlessBilling": "Yes",
      "PaymentMethod": "Electronic check",
      "MonthlyCharges": 29.85,
      "TotalCharges": 1889.50
    }
    ```
  - Salida enriquecida:
    ```json
    {
      "metadata": { "model_version": "v1.0", "timestamp": "2025-10-04T10:00:00Z" },
      "prediction": { "churn_probability": 0.742, "will_churn": 1, "risk_level": "Alto Riesgo", "confidence_score": 0.85 },
      "business_logic": { "suggested_action": "Retención Prioritaria / Oferta de Lealtad" },
      "prevision": "Va a cancelar",
      "probabilidad": 0.742,
      "top_features": ["Contract", "tenure", "OnlineSecurity"]
    }
    ```
- GET `/api/churn/stats`
  - Devuelve: `{ "total_evaluados": N, "tasa_churn": 0.xx }`

- POST `/api/churn/predict/batch` (JSON array)
  - Protegido: requiere `Authorization: Bearer <token>`
  - Entrada: lista de objetos como el ejemplo individual.
  - Salida: `{ items: [...], total: N, cancelaciones: M }`

- POST `/api/churn/predict/batch/csv` (multipart/form-data)
  - Protegido: requiere `Authorization: Bearer <token>`
  - Subir archivo CSV con encabezados canónicos (20 columnas): `gender,SeniorCitizen,Partner,Dependents,tenure,PhoneService,MultipleLines,InternetService,OnlineSecurity,OnlineBackup,DeviceProtection,TechSupport,StreamingTV,StreamingMovies,Contract,PaperlessBilling,PaymentMethod,MonthlyCharges,TotalCharges`
  - Nota: `TotalCharges` vacío se normaliza a `0.0` (Opción A); alternativamente puede rechazarse la solicitud (Opción B).
  - Ejemplo listo para usar: `samples/churn_batch_sample.csv`

- POST `/api/churn/evaluate/batch/csv` (multipart/form-data)
  - Protegido: requiere `Authorization: Bearer <token)`
  - Subir archivo CSV extendido que incluya las 20 columnas canónicas y adicionalmente `Churn` con valores `Yes`/`No`.
  - Salida: métricas y conteos `{ total, tp, tn, fp, fn, accuracy, precision, recall, f1 }`.

## Validación de entrada
- Campos requeridos para `/api/churn/predict` (20 variables):
  - Strings (sensibles a mayúsculas/minúsculas) deben coincidir exactamente con la referencia: 
    - `gender`: `Male|Female`
    - `Partner`, `Dependents`, `PhoneService`, `PaperlessBilling`: `Yes|No`
    - `MultipleLines`: `No|Yes|No phone service`
    - `InternetService`: `DSL|Fiber optic|No`
    - `OnlineSecurity`, `OnlineBackup`, `DeviceProtection`, `TechSupport`, `StreamingTV`, `StreamingMovies`: `Yes|No|No internet service`
    - `Contract`: `Month-to-month|One year|Two year`
    - `PaymentMethod`: `Electronic check|Mailed check|Bank transfer (automatic)|Credit card (automatic)`
  - Números:
    - `SeniorCitizen`: entero `0|1`
    - `tenure`: entero ≥ 0
    - `MonthlyCharges`, `TotalCharges`: números ≥ 0 sin símbolos de moneda.
  - Nulos:
    - `TotalCharges` vacío/nulo → se normaliza a `0.0` (Opción A) o se rechaza (Opción B).
  
- Errores 400 incluirán detalle por campo con claves coincidentes a los nombres canónicos.

## Ejemplos de petición y respuesta
- Postman: importar y usar [postman/ChurnInsight.postman_collection.json](postman/ChurnInsight.postman_collection.json).
- cURL (predicción individual):
  ```bash
  curl -X POST "http://localhost:8080/api/churn/predict" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <token>" \
    -d '{
      "gender": "Female",
      "SeniorCitizen": 0,
      "Partner": "Yes",
      "Dependents": "No",
      "tenure": 24,
      "PhoneService": "Yes",
      "MultipleLines": "No",
      "InternetService": "DSL",
      "OnlineSecurity": "Yes",
      "OnlineBackup": "No",
      "DeviceProtection": "No",
      "TechSupport": "No",
      "StreamingTV": "No",
      "StreamingMovies": "No",
      "Contract": "One year",
      "PaperlessBilling": "Yes",
      "PaymentMethod": "Electronic check",
      "MonthlyCharges": 29.85,
      "TotalCharges": 1889.50
    }'
  ```
- cURL (batch CSV):
  ```bash
  curl -X POST "http://localhost:8080/api/churn/predict/batch/csv" \
    -H "Content-Type: multipart/form-data" \
    -H "Authorization: Bearer <token>" \
    -F "file=@samples/churn_batch_sample.csv"
  ```

- cURL (evaluate CSV):
  ```bash
  curl -X POST "http://localhost:8080/api/churn/evaluate/batch/csv" \
    -H "Content-Type: multipart/form-data" \
    -H "Authorization: Bearer <token>" \
    -F "file=@<ruta-al-csv-extendido-con-Churn>.csv"
  ```

## Configuración
- `src/main/resources/application.properties`:
  - `churn.ds.url` → URL del servicio DS (ej. `http://localhost:8000/predict`).
  - Persistencia: se guardan predicciones en tabla `predictions` (H2 por defecto).

## Ejecución
### Inicio rápido (un comando)
Nota: `<project-root>` es la carpeta donde clonaste el proyecto (contiene `docker-compose.yml`). Ajusta la ruta según tu entorno.
```powershell
cd <project-root>
./run.ps1 -Build
```
- Levanta los servicios con Docker Compose, espera a que estén saludables, obtiene un JWT de admin y abre el dashboard.
- Usa `-Build` para forzar reconstrucción de imágenes.

```powershell
# En Windows
cd <project-root>
.\mvnw.cmd spring-boot:run
```

### JWT para endpoints protegidos
Los endpoints de predicción y estadísticas requieren autenticación. Por ejemplo, para consultar estadísticas:

```powershell
# 1) Login para obtener token (admin inicial)
$body = @{ email = "admin@local"; password = "Admin123!" } | ConvertTo-Json
$res = Invoke-RestMethod -Method POST -Uri http://localhost:8080/api/auth/login -ContentType "application/json" -Body $body
$token = $res.token

# 2) Consultar stats con Bearer token
Invoke-RestMethod -Method GET -Uri http://localhost:8080/api/churn/stats -Headers @{ Authorization = "Bearer $token" }
```

## Pruebas
```powershell
.\mvnw.cmd test
```

## Integración con DS
- Contrato del servicio DS (POST a `churn.ds.url`):
  - Entrada:
    ```json
    { "features": { /* 20 variables canónicas */ } }
    ```
  - Salida enriquecida y compatibilidad histórica:
    ```json
    {
      "metadata": {"model_version": "v1.0", "timestamp": "..."},
      "prediction": {"churn_probability": 0.742, "will_churn": 1, "risk_level": "Alto Riesgo", "confidence_score": 0.85},
      "business_logic": {"suggested_action": "Retención Prioritaria / Oferta de Lealtad"},
      "prevision": "Va a cancelar",
      "probabilidad": 0.742,
      "top_features": ["Contract", "tenure", "OnlineSecurity"]
    }
    ```

## Notebook (Data Science)
- Ver `notebooks/churn_modeling.ipynb` con EDA, entrenamiento y serialización del modelo (`joblib.dump`).

## Docker Compose (API + DS)
```powershell
cd <project-root>
docker compose up --build
```
- La API quedará en `http://localhost:8080`, el DS en `http://localhost:8000`.
- La API usa `.env` para `CHURN_DS_URL`, `JWT_SECRET`, etc.
- Para cargar un modelo entrenado, coloca los artefactos en `models/` (montado en `/models` del contenedor `ds`) y opcionalmente define `CHURN_MODEL_DIR=/models`.
- Healthchecks: `ds` y `dashboard` verifican sus puertos; `api` verifica el proceso de la app. El arranque del `dashboard` espera a que `api` esté saludable.

## Documentación de API
- Swagger UI (si `springdoc` está habilitado): `http://localhost:8080/swagger-ui/index.html`
- Health (Actuator): `http://localhost:8080/actuator/health`

## Postman Collection
- Importa `postman/ChurnInsight.postman_collection.json` y ejecuta los ejemplos incluidos.

## Dependencias y versiones
- Java: JDK 17 (propiedad `java.version`)
- Spring Boot: 3.4.12 (starters: web, validation, security, data-jpa)
- Base de datos: H2 (runtime)
- Utilidades: Lombok (opcional), springdoc-openapi 2.6.0, commons-csv 1.12.0, JJWT 0.11.5
- Testing: spring-boot-starter-test, spring-security-test
- Data Science (microservicio): Flask 3.0.3
- Build: Maven Wrapper (`mvnw`/`mvnw.cmd`)

## Explicación del modelo (DS actual)
El microservicio DS acepta las 20 variables canónicas y, en ausencia de un pipeline entrenado (`joblib`), aplica una heurística mínima basada en:
- `tenure` (reduce riesgo con antigüedad)
- `Contract` (Month-to-month aumenta riesgo; Two year reduce)
- `OnlineSecurity` (No aumenta riesgo)
- `MonthlyCharges` y `TotalCharges` (pequeño efecto)

La probabilidad se calcula con una función sigmoide y se devuelven `top_features` aproximadas. Cuando se disponga del modelo entrenado, DS podrá cargar `churn_pipeline.pkl` y `feature_names.pkl` para reemplazar la heurística sin cambiar el contrato.

## Nombre y alcance
- Nombre del proyecto: Churn Alert.
- Alcance: predicción de cancelación con probabilidad, explicabilidad (top features), endpoints individuales y por lotes (JSON/CSV), y persistencia de resultados para auditoría.

## Estado del proyecto (según requerimientos del hackathon)

- MVP (obligatorio)
  - [x] POST /api/churn/predict devuelve "prevision" y "probabilidad"
  - [x] Acceso al modelo vía microservicio DS (churn.ds.url / CHURN_DS_URL)
  - [x] Validación de entrada de campos obligatorios y errores 400 claros
  - [x] Respuesta estructurada
  - [x] Ejemplos de uso (Postman)
  - [x] Documentación simple (este README)

- Entregables de Data Science
  - [x] Notebook con EDA, ingeniería de features y entrenamiento
  - [x] Métricas (Accuracy, Precision, Recall, F1) documentadas en el notebook
  - [x] Serialización del modelo (joblib/pickle)

- Back-end (API REST)
  - [x] API Spring Boot con endpoints documentados
  - [x] Integración con el modelo de DS
  - [x] Logs y manejo de errores claros (pendiente CSV resuelto)

- Funcionalidades opcionales
  - [x] GET /api/churn/stats
  - [x] Persistencia de predicciones (H2 por defecto)
  - [x] Explicabilidad básica (top 3 features)
  - [x] Batch Prediction (JSON y CSV)
  - [x] Contenerización (Docker/Docker Compose)
  - [x] Dashboard simple (Streamlit/HTML)
  - [x] Pruebas automatizadas (unitarias e integración)

- Demostración funcional
  - [x] Colección Postman
  - [x] Breve explicación de cómo el modelo llega a la predicción (sección "Explicación del modelo")

### To-Do inmediato
- Agregar logs (INFO/ERROR) adicionales si se requiere trazabilidad más detallada.
- Documentar métricas del modelo en el notebook y enlazarlas aquí.
- Crear pruebas básicas (JUnit para API, pytest para DS).
- Opcional: dashboard simple para visualizar riesgos.

## Dashboard (Streamlit)

Un panel sencillo para interactuar con la API, realizar predicciones individuales, subir CSV por lotes y ver estadísticas.

### Requisitos
- Python 3.10+

### Instalación y ejecución (Windows PowerShell)
```powershell
cd <project-root>
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r dashboard/requirements.txt
streamlit run dashboard/app.py
```

### Autenticación en el dashboard
- Los endpoints protegidos (predicción y batch CSV) requieren JWT. Usa el panel lateral del dashboard ("Login rápido") para obtener el token con `admin@local` / `Admin123!`, o pega manualmente el token en el campo "Bearer token".
- Si intentas subir un CSV sin token, el dashboard mostrará una guía para iniciar sesión antes de continuar.
- Alternativa: ejecuta `./run.ps1 -Build` para levantar todo con Docker y abrir el dashboard; el script obtiene un JWT automáticamente y puedes copiarlo si lo necesitas.

### Uso
- Por defecto apunta a `http://localhost:8080`. Puedes ajustar la URL y (si aplica) ingresar un Bearer token en la barra lateral.
- Para prueba de lote, usa el CSV de ejemplo en [samples/churn_batch_sample.csv](samples/churn_batch_sample.csv).
- También se aceptan archivos extendidos que incluyan columnas adicionales como `customerID` y `Churn`; el backend ignora columnas no utilizadas. Asegúrate de incluir las 20 columnas canónicas con nombres exactos.
- Código del panel: [dashboard/app.py](dashboard/app.py) | Dependencias: [dashboard/requirements.txt](dashboard/requirements.txt)
