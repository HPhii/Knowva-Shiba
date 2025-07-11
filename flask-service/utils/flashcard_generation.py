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
You are a master academic assistant. Your goal is to create high-quality, effective flashcards in **{language}** from the provided text. A great flashcard isolates a single, important concept.

**Core Principles for a High-Quality Flashcard:**
1.  **Atomic:** One question on the front, one answer on the back. Avoid multiple questions or long lists.
2.  **Understandable:** The question must be clear without needing extra context.
3.  **Essential:** Focus on key terms, definitions, and core concepts, not trivial details.

**Your Task:**
-   Generate exactly **{max_flashcards}** flashcards.
-   The output must be a valid JSON object. Do NOT include any text outside the JSON.
-   If the text is too short or irrelevant, return an empty list: `{{"flashcards": []}}`

---

**Example:**

* **Input Text:** "The mitochondria is the powerhouse of the cell. It generates most of the cell's supply of adenosine triphosphate (ATP), used as a source of chemical energy. The process of cellular respiration occurs in the mitochondria."

* **Your JSON output:**
    ```json
    {{
      "flashcards": [
        {{
          "front": "What is the primary function of the mitochondria?",
          "back": "It is the 'powerhouse' of the cell, generating most of its chemical energy in the form of ATP.",
          "imageUrl": null
        }},
        {{
          "front": "What is ATP?",
          "back": "Adenosine triphosphate, the main source of chemical energy in a cell.",
          "imageUrl": null
        }}
      ]
    }}
    ```
---
"""
    elif card_type == "FILL_IN_THE_BLANK":
        SYSTEM_PROMPT = f"""
You are a master academic assistant. Your task is to create high-quality, fill-in-the-blank style flashcards in **{language}**.

**Core Principles for a High-Quality Fill-in-the-Blank Flashcard:**
1.  **Single Blank:** The front should have only one blank (`____`).
2.  **Context is Key:** The sentence on the front must provide enough context to logically deduce the answer on the back.
3.  **Concise Answer:** The back should contain *only* the word(s) that fill the blank, nothing more.

**Your Task:**
-   Generate exactly **{max_flashcards}** flashcards.
-   The output must be a valid JSON object. Do NOT include any text outside the JSON.
-   If the text is too short or irrelevant, return an empty list: `{{"flashcards": []}}`

---

**Example:**

* **Input Text:** "The Treaty of Versailles was the most important of the peace treaties that brought World War I to an end. The treaty was signed on 28 June 1919."

* **Your JSON output:**
    ```json
    {{
      "flashcards": [
        {{
          "front": "The Treaty of ____ was the most important peace treaty that brought World War I to an end.",
          "back": "Versailles",
          "imageUrl": null
        }},
        {{
          "front": "The Treaty of Versailles was signed on ____.",
          "back": "28 June 1919",
          "imageUrl": null
        }}
      ]
    }}
    ```
---
"""
    else:
        raise ValueError(f"Unsupported card type: {card_type}")

    completion = client2.chat.completions.create(
        model="nvidia/llama-3.1-nemotron-ultra-253b-v1",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text}
        ],
        temperature=0.5,  # Giảm nhẹ để AI bám sát quy tắc hơn
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