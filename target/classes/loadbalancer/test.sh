for i in {1..3}
do
  curl -s -X  GET http://localhost:1010 --data "Request $i"
  echo "Request $i sent"
done