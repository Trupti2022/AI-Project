# AI-Project


# Next_word_predictor

## Overview
This project is focused on building a **Next Word Prediction** model using content from Wikipedia articles, specifically related to the topic of **Artificial Intelligence**. The model is designed to predict the next word in a sequence of text input, making it useful for autocomplete systems or text generation.

The project utilizes Natural Language Processing (NLP) techniques to process the text data and train an LSTM-based model for the word prediction task.

## Project Workflow

1. **Data Collection**: 
   - The project uses the `wikipedia` Python package to scrape content from Wikipedia articles.
   - Specifically, content related to "Artificial Intelligence" is retrieved from the Wikipedia page.

2. **Data Preprocessing**:
   - The text is cleaned by removing special characters and formatting.
   - Sentences are tokenized and transformed into sequences of numbers using a tokenizer from TensorFlow's Keras library.

3. **Model Architecture**:
   - The model is built using an **LSTM** (Long Short-Term Memory) neural network, which is well-suited for sequence prediction tasks.
   - An **Embedding layer** is used to convert words into dense vectors, followed by an LSTM layer to capture word dependencies.
   - The model outputs a probability distribution over the vocabulary for the next predicted word.

4. **Training the Model**:
   - The model is trained on padded sequences of text, using categorical cross-entropy as the loss function and accuracy as a metric.
   - The model's predictions are evaluated during and after training to determine how well it can predict the next word.

5. **Next Word Prediction**:
   - After training, the model can take a sequence of words as input and predict the next word based on learned word relationships.
