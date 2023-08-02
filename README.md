# Dingo: A nature lover’s companion

Welcome to Dingo! Dingo is the CS 446 Project for Group 28 (Eden Chan, Philip Chen, Simhon Chourasia, Hitanshu Dalwadi, Austin Lin, Eric Shang, Dylan Xiao)

Our project is a mobile application where users can scan plants and animals around them and add them to a catalogue of flora and fauna they 
have seen before (called the DingoDex), tagged with the time and location that it was discovered, along with a picture taken by the user. The
app will use a pre-trained machine learning model to classify plants and animals from pictures. Users can add other users as friends to see
each other’s progress and each of their entries. There is also functionality for teachers and students, where teachers can create a classroom
and add their students to it. Students can use the classroom as a way to see their classmate’s DingoDex and trips, and teachers can see their
student’s progress. 

Enjoy!

### Testing

Follow these steps for testing the app locally: 

1. Install requirements for server: `django`, `PIL`, `tensorFlow`, `tensorflow_hub`, `numpy`
2. Find your ipv4 address (using ipconfig) and put it into `assets/setup.json` in the `serverIP` field with port `8000`
3. `cd` into `ml_photo_server` (from the root of this repo) and run `python manage.py runserver 192.168.0.0:8000`; replace `192.168.0.0:8000` with your own ip address and port number.
4. Ensure phone and server are on the same network (public networks might not work)
5. Download app on phone and test it out!
