# Image nhẹ, đã có Python & pip
FROM python:3.10-slim

# Cài Tesseract và các gói phụ thuộc
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    tesseract-ocr-vie \
    libtesseract-dev \
    poppler-utils \
    && rm -rf /var/lib/apt/lists/*

# Thiết lập biến môi trường Tesseract
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

# Tạo và chuyển vào thư mục app
WORKDIR /app

# Sao chép requirements và cài trước (cache tốt hơn)
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# Sao chép toàn bộ mã nguồn vào container
COPY . .

# Chạy ứng dụng
CMD ["gunicorn", "--workers", "3", "--bind", "0.0.0.0:5000", "app:app"]
