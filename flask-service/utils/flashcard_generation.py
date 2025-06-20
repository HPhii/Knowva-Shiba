import json
import re
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()
LLAMDA_API_KEY2 = os.getenv("LLAMDA_API_KEY2")  # For flashcard generation
client2 = OpenAI(base_url="https://integrate.api.nvidia.com/v1", api_key=LLAMDA_API_KEY2)

def generate_flashcards(text, language, card_type, max_flashcards):
    if card_type == "STANDARD":
        SYSTEM_PROMPT = f"""
You are an advanced academic assistant specialized in creating high-quality flashcards for educational purposes. Your task is to generate flashcards based on the provided input content. Each flashcard should have a front and a back, suitable for academic learning in the specified target language ({language}). The output must be structured as a JSON object matching the following format:

{{
    "flashcards": [
        {{
            "front": "Front text in {language}",
            "back": "Back text in {language}",
            "imageUrl": null
        }}
    ]
}}

Guidelines:
1. Generate exactly {max_flashcards} flashcards.
2. Ensure the content is factually accurate and relevant to the input content.
3. Use academic language appropriate for the target language ({language}).
4. Do not include images (set imageUrl to null).
5. If the input content is insufficient to generate {max_flashcards} flashcards, generate as many as possible.
"""
    elif card_type == "FILL_IN_THE_BLANK":
        SYSTEM_PROMPT = f"""
You are an advanced academic assistant specialized in creating high-quality fill-in-the-blank flashcards for educational purposes. Your task is to generate flashcards where the front has a sentence with a blank, and the back has the correct word or phrase to fill in the blank, suitable for academic learning in the specified target language ({language}). The output must be structured as a JSON object matching the following format:

{{
    "flashcards": [
        {{
            "front": "Sentence with a blank in {language}, e.g., 'The capital of France is ____.'",
            "back": "Correct word or phrase in {language}, e.g., 'Paris'",
            "imageUrl": null
        }}
    ]
}}

Guidelines:
1. Generate exactly {max_flashcards} flashcards.
2. Ensure the content is factually accurate and relevant to the input content.
3. Use academic language appropriate for the target language ({language}).
4. Do not include images (set imageUrl to null).
5. If the input content is insufficient to generate {max_flashcards} flashcards, generate as many as possible.
"""
    else:
        raise ValueError(f"Unsupported card type: {card_type}")

    completion = client2.chat.completions.create(
        model="nvidia/llama-3.1-nemotron-ultra-253b-v1",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text}
        ],
        temperature=0.6,
        top_p=0.95,
        max_tokens=4096,
        frequency_penalty=0,
        presence_penalty=0,
        stream=False
    )

    response_text = completion.choices[0].message.content
    json_match = re.search(r'({[\s\S]*})', response_text)
    if json_match:
        json_str = json_match.group(1)
        return json.loads(json_str)
    else:
        raise ValueError("Could not parse JSON response")