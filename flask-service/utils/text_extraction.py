import PyPDF2
from PIL import Image
import pytesseract

def extract_text(file, source_type):
    if source_type == "TEXT":
        return file.read().decode("utf-8")

    elif source_type == "IMAGE":
        image = Image.open(file)
        return pytesseract.image_to_string(image)

    elif source_type == "PDF":
        file.seek(0)
        try:
            pdf_reader = PyPDF2.PdfReader(file)
            text = ""
            for page in pdf_reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text
            return text
        except Exception as e:
            return f"[ERROR] Could not extract PDF text: {str(e)}"

    else:
        raise ValueError(f"Unsupported source type: {source_type}")
