# fung-eye

FungEye is a mobile mushroom identification app that leverages self-hosted local LLM for the identification process as well as the chatbot feature. The backend LLM server is required for this app to work. You can find the backend LLM server in this repository: [FungEyeAPI](https://github.com/duatonic/fung-eye-backend-llm-server.git).

The model used in this project is `gemma3:4b-it-qat`, though you can use any model you want as long as it can take image as input. You can also use separate model for the identification and the chatbot by configuring it on the backend app.