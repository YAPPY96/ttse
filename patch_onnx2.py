import onnx
from onnx import helper, numpy_helper
import numpy as np

def patch_model_v2(input_path, output_path):
    print(f"Loading {input_path}...")
    model = onnx.load(input_path)
    graph = model.graph

    # Store all original inputs
    original_inputs = {inp.name: inp for inp in graph.input}
    
    # We require EXACTLY these 6 inputs in this EXACT order for Sherpa-ONNX RunVits
    # 1. x_tst (int64)
    # 2. x_tst_lengths (int64)
    # 3. noise_scale (float)
    # 4. length_scale (float)
    # 5. noise_scale_w (float)
    # 6. sid (int64)
    
    desired_order = [
        "x_tst",
        "x_tst_lengths",
        "noise_scale",
        "length_scale",
        "noise_scale_w",
        "sid"
    ]
    
    new_inputs = []
    
    # Add ordered inputs
    for name in desired_order:
        if name in original_inputs:
            new_inputs.append(original_inputs[name])
        else:
            print(f"WARNING: Expected input {name} not found in model!")
            
    # For any other inputs, we remove them and turn them into zero constants!
    for name, inp in original_inputs.items():
        if name not in desired_order:
            shape = []
            for dim in inp.type.tensor_type.shape.dim:
                shape.append(dim.dim_value if dim.dim_value > 0 else 1)
                
            dtype = np.float32
            if inp.type.tensor_type.elem_type == onnx.TensorProto.FLOAT16:
                dtype = np.float16
            elif inp.type.tensor_type.elem_type == onnx.TensorProto.INT64:
                dtype = np.int64
                
            if name == 'sdp_ratio':
                zero_tensor = np.array(0.2, dtype=dtype) # typical sdp ratio
            else:
                zero_tensor = np.zeros(shape, dtype=dtype)
                
            init_tensor = numpy_helper.from_array(zero_tensor, name=name)
            graph.initializer.append(init_tensor)
            print(f"Moved {name} to initializer with shape {shape} and type {dtype}.")

    # Replace graph inputs
    del graph.input[:]
    graph.input.extend(new_inputs)

    print(f"Saving modified model to {output_path}...")
    onnx.save(model, output_path)
    print("Done! New inputs order is:")
    for idx, i in enumerate(model.graph.input):
        print(f"  {idx}: {i.name}")

if __name__ == "__main__":
    patch_model_v2("app/src/main/assets/style-bert-vits2-ja/model.onnx", "app/src/main/assets/style-bert-vits2-ja/model.onnx")
