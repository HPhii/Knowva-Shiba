import PyPDF2
from PIL import Image
import pytesseract

MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB

def extract_text(file, source_type):
    file.seek(0, 2)
    if file.tell() > MAX_FILE_SIZE:
        raise ValueError(f"File too large. Maximum size is {MAX_FILE_SIZE / (1024 * 1024)}MB")
    file.seek(0)

    if source_type == "TEXT":
        return file.read().decode("utf-8")
    elif source_type == "IMAGE":
        if file.content_type not in ["image/jpeg", "image/png"]:
            raise ValueError("Invalid image format. Only JPEG and PNG are supported.")
        with Image.open(file) as image:
            return pytesseract.image_to_string(image, lang="eng+vie")
    elif source_type == "PDF":
        if file.content_type != "application/pdf":
            raise ValueError("Invalid file format. Only PDF is supported.")
        try:
            with file.stream as f:
                pdf_reader = PyPDF2.PdfReader(f)
                text = ""
                for page in pdf_reader.pages:
                    page_text = page.extract_text()
                    if page_text:
                        text += page_text
                return text
        except Exception as e:
            raise ValueError(f"Could not extract PDF text: {str(e)}")
    else:
        raise ValueError(f"Unsupported source type: {source_type}")
