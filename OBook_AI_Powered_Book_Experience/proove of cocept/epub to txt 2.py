import requests
import time
import shutil

headers = {"Authorization": "2e0c1bf08a414b29aa6426ebb429cdd5"}
file_list = ["/Users/timxu/Downloads/output.epub"]
params = {
    "lang": "en",
    "convert_to": "epub-text",
    "ocr": False
}


def download_file(url, local_filename, api_url):
    url = f"{api_url}/{url.strip('/')}"

    with requests.get(url, stream=True) as r:
        with open(local_filename, "wb") as f:
            shutil.copyfileobj(r.raw, f)

    return local_filename


def convert_files(params, headers):
    r = requests.get(
        url="https://www.epub.to/apis/"
    )

    if r.status_code != 200:
        print("We couldn't get the API name")
        print(r.status_code)
        print(r.content)
        return None, None

    api_url = r.json().get("api")
    files = [eval(f'("files", open("{file}", "rb"))') for file in file_list]
    r = requests.post(
        url=f"{api_url}/v1/convert/",
        files=files,
        data=params,
        headers=headers
    )

    if r.status_code != 200:
        print(r.status_code)
        print(r.content)
        return None, None

    return r.json(), api_url


def get_results(params, api_url):
    if params.get("error"):
        print(params)
        return params.get("error")

    while True:
        r = requests.post(
            url=f"{api_url}/v1/results/",
            data=params
        )
        data = r.json()
        finished = data.get("finished")

        if not finished:
            if int(data.get("queue_count")) > 0:
                print("queue: %s" % data.get("queue_count"))

            time.sleep(5)
        else:
            for f in data.get("files"):
                print(f.get("url"))
                download_file(f.get("url"), f.get("filename"), api_url)
            break

    return {"finished": "files downloaded"}


resp, api_url = convert_files(params, headers)
get_results(resp, api_url)
