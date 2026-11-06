
## Almacenamiento de los archivos usar almacenamiento externo

En el application-prod.yml
 app:
    storage-dir: ${STORAGE_DIR}

## Para el MVP:
 STORAGE_DIR=/alguna/ruta/persistente

Para una versión productiva seria:
PostgreSQL
+
Amazon S3
o
PostgreSQL
+
Azure Blob Storage

