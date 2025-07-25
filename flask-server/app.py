from flask import Flask, jsonify,request
from predict_by_category import generate_reports, generate_category_line_chart, generate_503020

app = Flask(__name__)

@app.route("/report/503020",methods=["GET"])
def calculator_chart():
    try:
        need=float(request.args.get("need",0)) #read parameters from url
        want=float(request.args.get("want",0))
        saving=float(request.args.get("saving",0))
    except ValueError:
        return jsonify({"error": "Invalid parameters"}), 400

    chart=generate_503020(need,want,saving)
    if chart is None:
         return jsonify({"message": "No data"}), 204
    return jsonify({"chart":chart}) #sends back to Android a JSON response containing the image in Base64 format
    


@app.route("/report", methods=["GET"])
def report():
    result = generate_reports()
    return jsonify(result)

@app.route("/report/<path:category>", methods=["GET"])
def category_report(category):
    chart = generate_category_line_chart(category)
    if chart is None:
        return jsonify({"message": "No data for this category"}), 204
    return jsonify({"line_chart": chart})

if __name__ == "__main__":
    app.run(debug=True)
