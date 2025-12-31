# Churn Alert

## Objetivo
API REST que recibe datos de cliente y devuelve predicción de churn y probabilidad. Integra con un microservicio de Data Science (DS).

## Endpoints
- POST `/api/churn/predict` (JSON)
  - Entrada:
    ```json
    {
      "tiempo_contrato_meses": 12,
      "retrasos_pago": 2,
      "uso_mensual": 14.5,
      "plan": "Premium"
    }
    ```
  - Salida:
    ```json
    {
      "prevision": "Va a cancelar",
      "probabilidad": 0.76,
      "topFeatures": ["retrasos_pago", "tiempo_contrato_meses", "uso_mensual"],
      "timestamp": "2025-12-25T00:00:00Z"
    }
    ```
- GET `/api/churn/stats`
  - Devuelve: `{ "total_evaluados": N, "tasa_churn": 0.xx }`

- POST `/api/churn/predict/batch` (JSON array)
  - Entrada: lista de objetos como el ejemplo individual.
  - Salida: `{ items: [...], total: N, cancelaciones: M }`

- POST `/api/churn/predict/batch/csv` (multipart/form-data)
  - Subir archivo CSV con encabezados: `tiempo_contrato_meses,retrasos_pago,uso_mensual,plan`
  - Salida igual al batch JSON.
  - Ejemplo listo para usar: `samples/churn_batch_sample.csv`

## Validación de entrada
- Campos requeridos para `/api/churn/predict`:
  - `tiempo_contrato_meses`: entero ≥ 0
  - `retrasos_pago`: entero ≥ 0
  - `uso_mensual`: número ≥ 0
  - `plan`: string entre {"Basic","Standard","Premium"} (no sensible a mayúsculas/minúsculas)
- Respuestas esperadas en caso de error:
  - HTTP 400 con detalle por campo, por ejemplo:
    ```json
    {
      "errors": {
        "tiempo_contrato_meses": "Debe ser un entero no negativo",
        "plan": "Valor inválido: use Basic/Standard/Premium"
      }
    }
    ```
Nota: la validación ya está implementada en la API (ver pruebas en `target/surefire-reports`).

## Ejemplos de petición y respuesta
- Postman: importar y usar [postman/ChurnInsight.postman_collection.json](postman/ChurnInsight.postman_collection.json).
- cURL (predicción individual):
  ```bash
  curl -X POST "http://localhost:8080/api/churn/predict" \
    -H "Content-Type: application/json" \
    -d '{
      "tiempo_contrato_meses": 12,
      "retrasos_pago": 2,
      "uso_mensual": 14.5,
      "plan": "Premium"
    }'
  ```
- cURL (batch CSV):
  ```bash
  curl -X POST "http://localhost:8080/api/churn/predict/batch/csv" \
    -H "Content-Type: multipart/form-data" \
    -F "file=@samples/churn_batch_sample.csv"
  ```

## Configuración
- Entorno (`.env`) y propiedades (`src/main/resources/application.properties`):
  - `CHURN_DS_URL` → URL del servicio DS (ej. `http://localhost:8000/predict`). Si está vacío, la API usa heurístico local.
  - `JWT_SECRET` → secreto para firmar tokens JWT.
  - `JWT_EXPIRATION_MINUTES` → minutos de validez del JWT.
  - `ADMIN_INITIAL_EMAIL` / `ADMIN_INITIAL_PASSWORD` / `ADMIN_INITIAL_FULL_NAME` → usuario admin inicial (solo dev).
  - Persistencia: H2 (memoria) por defecto.

## Ejecución
### Inicio rápido (un comando)
```powershell
cd "c:\Users\hugow\Hackaton Alura\hackatonAlura"
./run.ps1 -Build
```
- Levanta los servicios con Docker Compose, espera a que estén saludables, obtiene un JWT de admin y abre el dashboard.
- Usa `-Build` para forzar reconstrucción de imágenes.

```powershell
# En Windows
cd "c:\Users\hugow\Hackaton Alura\hackatonAlura"
.\mvnw.cmd spring-boot:run
```

### JWT para endpoints protegidos
El endpoint de estadísticas (`/api/churn/stats`) requiere autenticación. Para consultarlo:

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
- Contrato esperado del servicio DS (POST a `churn.ds.url`):
  - Entrada:
    ```json
    {
      "features": {
        "tiempo_contrato_meses": 12,
        "retrasos_pago": 2,
        "uso_mensual": 14.5,
        "plan": "Premium"
      }
    }
    ```
  - Salida (heurístico o modelo entrenado):
    ```json
    { "prevision": "Va a cancelar", "probabilidad": 0.81, "top_features": ["retrasos_pago","plan","tiempo_contrato_meses"] }
    ```

## Notebook (Data Science)
- Ver `notebooks/churn_modeling.ipynb` con EDA, entrenamiento y serialización del modelo (`joblib.dump`).

## Docker Compose (API + DS)
```powershell
cd "c:\Users\hugow\Hackaton Alura\hackatonAlura"
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
El microservicio DS implementa un puntaje heurístico que aproxima una función logística sobre cuatro señales:
- `retrasos_pago` (aumenta el riesgo)
- `tiempo_contrato_meses` (disminuye el riesgo con antigüedad)
- `uso_mensual` (mayor uso disminuye el riesgo)
- `plan` (riesgo base según tipo de plan)

Se calcula un puntaje lineal y se transforma en probabilidad con la función sigmoide. Además, se retornan `top_features` como las tres variables con mayor contribución absoluta al resultado. Cuando esté disponible un modelo entrenado (joblib/pickle), el microservicio puede cargarlo para reemplazar la heurística sin cambiar el contrato de integración.

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
cd "c:\Users\hugow\Hackaton Alura\hackatonAlura"
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r dashboard/requirements.txt
streamlit run dashboard/app.py
```

### Uso
- Por defecto apunta a `http://localhost:8080`. Puedes ajustar la URL y (si aplica) ingresar un Bearer token en la barra lateral.
- Para prueba de lote, usa el CSV de ejemplo en [samples/churn_batch_sample.csv](samples/churn_batch_sample.csv).
- Código del panel: [dashboard/app.py](dashboard/app.py) | Dependencias: [dashboard/requirements.txt](dashboard/requirements.txt)
