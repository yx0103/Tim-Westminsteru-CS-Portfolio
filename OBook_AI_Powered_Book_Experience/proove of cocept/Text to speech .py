import requests
import subprocess
import json

# Step 1: Get the Google Cloud Project ID
def get_project_id():
    result = subprocess.run(
        ["gcloud", "config", "list", "--format=value(core.project)"],
        capture_output=True, text=True
    )
    return result.stdout.strip()

# Step 2: Get the Access Token
def get_access_token():
    result = subprocess.run(
        ["gcloud", "auth", "print-access-token"],
        capture_output=True, text=True
    )
    return result.stdout.strip()

# Step 3: Prepare the API request
def synthesize_speech(text, language_code, voice_name, audio_encoding):
    url = "https://texttospeech.googleapis.com/v1/text:synthesize"

    headers = {
        "Content-Type": "application/json",
        "X-Goog-User-Project": get_project_id(),
        "Authorization": f"Bearer {get_access_token()}",
    }

    data = {
        "input": {
            "text": text
        },
        "voice": {
            "languageCode": language_code,
            "name": voice_name
        },
        "audioConfig": {
            "audioEncoding": audio_encoding
        }
    }

    # Step 4: Make the POST request
    response = requests.post(url, headers=headers, json=data)

    if response.status_code == 200:
        print("Request successful!")
        response_data = response.json()
        audio_content = response_data.get("audioContent")

        # Save the audio to a file
        with open("output_audio.wav", "wb") as audio_file:
            audio_file.write(base64.b64decode(audio_content))
        print("Audio content written to 'output_audio.wav'.")
    else:
        print(f"Error {response.status_code}: {response.text}")

# Step 5: Execute the function
if __name__ == "__main__":
    import base64

    synthesize_speech(
        text="hello my name is tim.",
        language_code="en-US",
        voice_name="en-US-Standard-A",
        audio_encoding="LINEAR16"
    )