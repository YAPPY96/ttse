import onnx

def add_metadata(model_path, output_path, metadata):
    model = onnx.load(model_path)
    while len(model.metadata_props) > 0:
        model.metadata_props.pop()
    for key, value in metadata.items():
        meta = model.metadata_props.add()
        meta.key = key
        meta.value = str(value)
    onnx.save(model, output_path)

metadata = {
    "sample_rate": "44100",
    "n_speakers": "1",
    "model_type": "vits",
    "comment": "style-bert-vits2",
    "language": "Japanese",
    "voice": "ja-jp",
    "has_espeak": "0",
    "is_style_bert_vits2": "1",
}

add_metadata("app/src/main/assets/style-bert-vits2-ja/model.onnx", "app/src/main/assets/style-bert-vits2-ja/model.onnx", metadata)
print("Metadata updated with has_espeak=0.")
