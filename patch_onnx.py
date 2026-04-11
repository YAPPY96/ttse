import onnx
from onnx import helper, numpy_helper
import numpy as np

def patch_model(input_path, output_path):
    print(f"Loading {input_path}...")
    model = onnx.load(input_path)
    graph = model.graph

    # Inputs we want to remove from graph.input and replace with zero initializers
    inputs_to_remove = ['bert', 'ja_bert', 'en_bert', 'style_vec']
    
    new_inputs = []
    removed_names = []

    for inp in graph.input:
        if inp.name in inputs_to_remove:
            removed_names.append(inp.name)
            
            # Determine shape
            shape = []
            for dim in inp.type.tensor_type.shape.dim:
                # If dimension is dynamic (0 or 'None'), we use a small size like 1
                if dim.dim_value > 0:
                    shape.append(dim.dim_value)
                else:
                    # e.g., seq_len or batch_size
                    shape.append(1)
                    
            if inp.name == 'style_vec':
                shape = [1, 256]
            else:
                shape = [1, 1024, 1]
                
            # Determine type
            dtype = np.float32
            if inp.type.tensor_type.elem_type == onnx.TensorProto.FLOAT16:
                dtype = np.float16
                
            # Create a zero tensor
            zero_tensor = np.zeros(shape, dtype=dtype)
            
            # Create ONNX tensor
            init_tensor = numpy_helper.from_array(zero_tensor, name=inp.name)
            
            # Add to initializers
            graph.initializer.append(init_tensor)
            print(f"Moved {inp.name} to initializer with shape {shape} and type {dtype}.")
        else:
            new_inputs.append(inp)
            
    # Also check if 'language' is supported by Sherpa, if not, we should probably also dummy it.
    # But Sherpa-ONNX docs say they support 'language' for VITS if it's there. 

    # Replace graph inputs
    del graph.input[:]
    graph.input.extend(new_inputs)

    print(f"Saving modified model to {output_path}...")
    onnx.save(model, output_path)
    print("Done! Remaining inputs:")
    for i in model.graph.input:
        print("  -", i.name)

if __name__ == "__main__":
    patch_model("app/src/main/assets/style-bert-vits2-ja/model.onnx", "app/src/main/assets/style-bert-vits2-ja/model.onnx")
