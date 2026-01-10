import requests

ticket = {
    "ticketId": "TKT-001",
    "title": "Cannot login after password reset",
    "description": "User tries to login but keeps failing, error code 401",
    "priority": "HIGH"
}

response = requests.post("http://127.0.0.1:5000/classify", json=ticket)

print("Status code:", response.status_code)
print("\nResponse:")

try:
    result = response.json()
    if "error" in result:
        print(f"❌ Error: {result['error']}")
    else:
        print(f"✅ Ticket: {result['ticketId']}")
        print(f"✅ Category: {result['categoryName']} ({result['predictedCategory']})")
        print(f"✅ Confidence: {result['confidence']:.2%}")
        print(f"\nAlternatives:")
        for alt in result['alternatives']:
            print(f"  - {alt['categoryName']}: {alt['confidence']:.2%}")
except Exception as e:
    print("❌ Failed to parse JSON:", e)
    print("Raw response:", response.text)