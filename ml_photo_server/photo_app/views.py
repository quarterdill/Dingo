from django.shortcuts import render

# Create your views here.
from django.http import JsonResponse
from django.conf import settings
from PIL import Image
from io import BytesIO
import tensorflow_hub as hub
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
        print(model_path)
        self.interpreter = tf.lite.Interpreter(model_path=model_path)
        self.interpreter.allocate_tensors()
        self.interpreter.resize_tensor_input(0, [1, 320, 320, 3])
        self.interpreter.allocate_tensors()
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()
        self.plant_model = tf.keras.Sequential([hub.KerasLayer("https://hub.tensorflow.google.cn/google/aiy/vision/classifier/plants_V1/1")])
        self.plant_labels = {'13': 'Fagus grandifolia', '64': 'Viburnum dentatum', '71': 'Rosa palustris', '168': 'Betula alleghaniensis', '193': 'Cornus sericea', '250': 'Parthenocissus quinquefolia', '315': 'Juniperus virginiana', '363': 'Amelanchier alnifolia', '372': 'Trillium erectum', '398': 'Acer rubrum', '409': 'Maianthemum racemosum', '525': 'Mertensia virginica', '577': 'Vaccinium angustifolium', '579': 'Picea glauca', '604': 'Erythronium americanum', '615': 'Eutrochium maculatum', '626': 'Hamamelis virginiana', '662': 'Populus deltoides', '741': 'Pinus strobus', '767': 'Acer saccharum', '781': 'Fragaria virginiana', '974': 'Monarda fistulosa', '1071': 'Aquilegia canadensis', '1089': 'Ulmus americana', '1204': 'Thuja occidentalis', '1254': 'Abies balsamea', '1399': 'Quercus alba', '1426': 'Prunus serotina', '1431': 'Salix nigra', '1551': 'Cornus canadensis', '1561': 'Aralia nudicaulis', '1722': 'Anemone canadensis', '1764': 'Arisaema triphyllum', '1795': 'Geranium maculatum', '1802': 'Lobelia cardinalis', '1808': 'Lindera benzoin', '1817': 'Asarum canadense', '1889': 'Fraxinus americana', '1905': 'Cercis canadensis', '1912': 'Corylus americana', '1945': 'Echinacea purpurea', '1969': 'Iris versicolor', '1971': 'Tsuga canadensis', '2037': 'Lilium canadense', '2057': 'Quercus rubra'}

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

@csrf_exempt
def process_plant(request):
    if not hasattr(process_plant, '_tflite_model'):
        model_path = os.path.join(settings.STATIC_ROOT, 'model.tflite')
        label_path = os.path.join(settings.STATIC_ROOT, 'labels.pbtxt')
        process_plant._tflite_model = TFLiteModelSingleton(model_path, label_path)
    if request.method == 'POST' and request.FILES['image']:
        plant_model = process_plant._tflite_model
        plant_interpreter = plant_model.plant_model
        image_file = request.FILES['image']
        image = Image.open(image_file)
        resized_image = load_and_preprocess_image(image)
        predictions = plant_interpreter.predict(resized_image)
        top_predictions = np.argsort(predictions[0])[::-1][:5]
        confidence = [predictions[0][i] for i in top_predictions]
        print(top_predictions)
        print(confidence)
        for i in confidence:
            if confidence[int(i)] >= 0.3 and str(top_predictions[int(i)]) in plant_model.plant_labels.keys():
                return JsonResponse({'processed_image': plant_model.plant_labels[str(top_predictions[int(i)])]})
        return JsonResponse({'processed_image': "not found"})
        
def load_and_preprocess_image(inputImage, target_size=(224, 224)):
    image = inputImage.resize(target_size)
    image = np.array(image) / 255.0  # Normalize to [0, 1]
    image = image[np.newaxis, ...]   # Add batch dimension
    return image