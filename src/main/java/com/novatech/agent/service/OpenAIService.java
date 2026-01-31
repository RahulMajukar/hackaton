    package com.novatech.agent.service;

    import com.theokanning.openai.embedding.EmbeddingRequest;
    import com.theokanning.openai.service.OpenAiService;
    import com.theokanning.openai.completion.chat.ChatCompletionRequest;
    import com.theokanning.openai.completion.chat.ChatMessage;
    import com.theokanning.openai.completion.chat.ChatMessageRole;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import jakarta.annotation.PostConstruct;
    import java.time.Duration;
    import java.util.ArrayList;
    import java.util.List;

    @Service
    public class OpenAIService {

        @Value("${openai.api-key}")
        private String apiKey;

        private OpenAiService openAi;

        @PostConstruct
        public void init() {
            this.openAi = new OpenAiService(apiKey, Duration.ofSeconds(60));
        }

        public float[] createEmbedding(String text) {
            try {
                if (text == null || text.trim().isEmpty()) {
                    return new float[1536]; // Return zero vector for empty text
                }

                // Truncate text if too long (OpenAI limit)
                String truncatedText = text.length() > 8192 ? text.substring(0, 8192) : text;

                EmbeddingRequest request = EmbeddingRequest.builder()
                        .model("text-embedding-3-small")
                        .input(List.of(truncatedText))
                        .build();

                var result = openAi.createEmbeddings(request);
                List<Double> embeddingDoubles = result.getData().get(0).getEmbedding();

                // Convert List<Double> to float[]
                float[] embeddingFloats = new float[embeddingDoubles.size()];
                for (int i = 0; i < embeddingDoubles.size(); i++) {
                    embeddingFloats[i] = embeddingDoubles.get(i).floatValue();
                }

                return embeddingFloats;

            } catch (Exception e) {
                System.err.println("Error creating embedding: " + e.getMessage());
                e.printStackTrace();
                return new float[1536]; // Return zero vector as fallback
            }
        }

        public String generateAnswer(String question, List<String> contexts) {
            try {
                if (contexts == null || contexts.isEmpty()) {
                    return "I couldn't find relevant information in the knowledge base to answer this question.";
                }

                // Build prompt
                StringBuilder prompt = new StringBuilder();
                prompt.append("You are NovaTech's internal knowledge assistant. ");
                prompt.append("Answer the user's question using ONLY the information from the provided sources. ");
                prompt.append("Cite sources using [Source X] notation.\n\n");

                prompt.append("Question: ").append(question).append("\n\n");

                prompt.append("Sources:\n");
                for (int i = 0; i < contexts.size(); i++) {
                    prompt.append("[Source ").append(i + 1).append("]: ")
                            .append(contexts.get(i)).append("\n\n");
                }

                prompt.append("Instructions:\n");
                prompt.append("1. Answer based ONLY on the sources above\n");
                prompt.append("2. Cite sources like [Source 1], [Source 2]\n");
                prompt.append("3. If the answer is not in the sources, say 'I don't have enough information'\n");
                prompt.append("4. Be concise and accurate\n");

                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                        "You are a helpful assistant that answers questions based on provided sources."));
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt.toString()));

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        // .model("gpt-3.5-turbo")
                        .model("gpt-4-turbo")
                        .messages(messages)
                        .temperature(0.1)
                        .maxTokens(500)
                        .build();

                var result = openAi.createChatCompletion(request);
                return result.getChoices().get(0).getMessage().getContent();

            } catch (Exception e) {
                System.err.println("Error generating answer: " + e.getMessage());
                e.printStackTrace();
                return "Error generating answer. Please try again.";
            }
        }
    }