from django.shortcuts import render

# Create your views here.
from django.http import JsonResponse
from django.conf import settings
from PIL import Image
from io import BytesIO
import tensorflow as tf
import numpy as np
from django.views.decorators.csrf import csrf_exempt
import os
import imageio
import re



# Example usage:
class TFLiteModelSingleton:
    _instance = None

    def __new__(cls, model_path, label_path):
        if cls._instance is None:
            cls._instance = super(TFLiteModelSingleton, cls).__new__(cls)
            cls._instance.initialize_interpreter(model_path, label_path)
        return cls._instance

    def initialize_interpreter(self, model_path, label_path):
        self.labels = {}
        with open(label_path, 'r') as pbtxt_file:
            # Read the entire content of the .pbtxt file
            pbtxt_content = pbtxt_file.read()
        # Use regular expressions to extract the id and name fields from each item
        item_regex = r'item {\s*name: "([^"]+)"\s*id: (\d+)\s*display_name: "([^"]+)"\s*}'
        matches = re.findall(item_regex, pbtxt_content)
        # Populate the result dictionary with id as keys and name as values
        for item in matches:
            useless, id, name_value = item
            self.labels[int(id)] = name_value
        self.interpreter = tf.lite.Interpreter(model_path=model_path)
        self.interpreter.allocate_tensors()
        self.interpreter.resize_tensor_input(0, [1, 320, 320, 3])
        self.interpreter.allocate_tensors()
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()

@csrf_exempt
def process_image(request):
    if not hasattr(process_image, '_tflite_model'):
        model_path = os.path.join(settings.STATIC_ROOT, 'model.tflite')
        label_path = os.path.join(settings.STATIC_ROOT, 'labels.pbtxt')
        process_image._tflite_model = TFLiteModelSingleton(model_path, label_path)
    if request.method == 'POST' and request.FILES['image']:
        tflite_model = process_image._tflite_model
        interpreter = tflite_model.interpreter
        input_details = tflite_model.input_details
        output_details = tflite_model.output_details
        image_file = request.FILES['image']
        image = Image.open(image_file)
        bitmap = image.convert("RGB")
        test = np.expand_dims(bitmap, axis=0)
        interpreter.set_tensor(input_details[0]['index'], test )
        interpreter.invoke()
        species = interpreter.get_tensor(output_details[1]['index']).flatten().tolist()
        confidence = interpreter.get_tensor(output_details[2]['index']).flatten().tolist()
        print(species)
        print(confidence)
        for i in range(len(confidence)):
            if confidence[i] > 0.7 and int(species[i]) in tflite_model.labels.keys() :
                animal = tflite_model.labels[int(species[i])]
                return JsonResponse({'processed_image': animal})
        return JsonResponse({'processed_image': "not found"})

    return JsonResponse({'processed_image': "harhahr"})