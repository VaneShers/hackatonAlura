Place trained model artifacts here for the DS service.

Expected files:
- churn_pipeline.pkl
- feature_names.pkl

Compose mounts this directory at /models inside the ds-service container.
Set CHURN_MODEL_DIR if you use a different path.