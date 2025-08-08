from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/analyze_skin', methods=['POST'])
def analyze_skin():
    if 'image' not in request.files:
        return jsonify({"error": "No image file provided"}), 400

    file = request.files['image']
    # You can save the file here for inspection if needed
    # file.save("received_image.jpg")

    print("Image received. Performing dummy analysis...")
    
    # --- Dummy Analysis Response (to be replaced by your ML model) ---
    skin_analysis_result = {
        "skin_type": "Oily",
        "concerns": ["Acne", "Large Pores"],
        "product_recommendations": [
            {"name": "Salicylic Acid Cleanser", "brand": "Brand A"},
            {"name": "Niacinamide Serum", "brand": "Brand B"}
        ],
        "diet_recommendations": [
            "Reduce sugary foods and dairy.",
            "Eat more foods rich in Omega-3 fatty acids (like salmon and walnuts)."
        ]
    }
    
    return jsonify(skin_analysis_result), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)