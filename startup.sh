cd /home/ec2-user
aws s3 cp s3://quickflick-buckeet/builds/quickflick-0.0.1-SNAPSHOT.jar .
java -jar quickflick-0.0.1-SNAPSHOT.jar
