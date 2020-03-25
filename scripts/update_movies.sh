#!/bin/bash

echo "--- ENTERING datasets/ ---"
cd datasets/

echo "--- DOWNLOADING ---"

wget -r -nH --cut-dirs -A "*.tsv.gz" https://datasets.imdbws.com

echo "--- EXITING datasets/ ---"
cd ..

echo "--- RUNNING PARSER ---"
python3 parse_dataset.py

echo "--- UPLOADING MOVIES ---"
python3 upload_movies.py
