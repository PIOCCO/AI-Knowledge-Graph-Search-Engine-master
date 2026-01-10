from flask import Flask, request, jsonify
import joblib
import numpy as np
from datetime import datetime

app = Flask(__name__)

# Load model, vectorizer, and label encoder
try:
    model = joblib.load("ticket_classifier.pkl")
    vectorizer = joblib.load("tfidf_vectorizer.pkl")
    label_encoder = joblib.load("label_encoder.pkl")  # ✅ Load the encoder
    print("✅ Model, vectorizer, and label encoder loaded successfully")
    
    # Display the category mapping
    print("\nCategory mapping:")
    for idx, label in enumerate(label_encoder.classes_):
        print(f"  {idx}: {label}")
        
except Exception as e:
    print("❌ Failed to load model/vectorizer/encoder:", e)
    raise

# ✅ Create proper mapping using the label encoder
CATEGORY_MAPPING = {
    i: f"CAT-{label_encoder.classes_[i][:4].upper()}" 
    for i in range(len(label_encoder.classes_))
}

@app.route("/classify", methods=["POST"])
def classify():
    try:
        data = request.get_json(force=True)
        print("Received request:", data.get("ticketId"))
        
        # Combine title and description
        text = data["title"] + " " + data["description"]
        
        # Transform and predict
        features = vectorizer.transform([text])
        pred = model.predict(features)[0]  # This returns an integer
        probs = model.predict_proba(features)[0]
        
        # Get category name from label encoder
        category_name = label_encoder.classes_[pred]
        category_id = CATEGORY_MAPPING.get(pred, f"CAT-{pred}")
        
        # Get alternatives (top 3 excluding the prediction)
        top_indices = np.argsort(probs)[::-1]
        alternatives = [
            {
                "categoryId": CATEGORY_MAPPING.get(i, f"CAT-{i}"),
                "categoryName": label_encoder.classes_[i],
                "confidence": float(probs[i])
            }
            for i in top_indices[1:3]  # Get 2nd and 3rd best
        ]
        
        result = {
            "ticketId": data["ticketId"],
            "predictedCategory": category_id,
            "categoryName": category_name,
            "confidence": float(probs[pred]),
            "alternatives": alternatives,
            "timestamp": datetime.now().isoformat()
        }
        
        print(f"✅ Classified: {category_name} (confidence: {probs[pred]:.2%})")
        return jsonify(result)
        
    except Exception as e:
        print("❌ Error in classify:", e)
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5000, debug=True)