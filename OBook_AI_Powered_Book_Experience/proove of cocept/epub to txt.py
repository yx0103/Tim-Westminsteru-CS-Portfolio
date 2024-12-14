import subprocess
import os

def convert_epub(input_file, output_file):
    """
    Converts an EPUB file to a simpler EPUB format using ebook-convert.
    :param input_file: Path to the input EPUB file.
    :param output_file: Path to the output EPUB file.
    """
    # Check if Calibre's tool is installed
    try:
        subprocess.run(["ebook-convert", "--version"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except FileNotFoundError:
        print("Error: Calibre's 'ebook-convert' tool is not installed or not in PATH.")
        return

    # Run the ebook-convert command
    try:
        subprocess.run(["ebook-convert", input_file, output_file], check=True)
        print(f"Conversion successful! Output file: {output_file}")
    except subprocess.CalledProcessError as e:
        print("Error during conversion:", e)

# Example usage
input_epub = "/Users/timxu/Downloads/3(1).epub"
output_epub = "/Users/timxu/Downloads/output.epub"

# Ensure the paths exist
if os.path.exists(input_epub):
    convert_epub(input_epub, output_epub)
else:
    print("Input EPUB file does not exist.")