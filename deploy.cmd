call gcloud functions deploy java-http-function --gen2 --entry-point=func.HelloWorld --runtime=java21 --region=europe-west10 --source=./target --trigger-http --allow-unauthenticated

pause

rem see https://cloud.google.com/functions/docs/create-deploy-http-java?hl=de