from django.urls import path
from . import views

urlpatterns = [
    path('process_image/', views.process_image, name='process_image'),
    path('process_plant/', views.process_plant, name='process_plant')
]