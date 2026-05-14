package ru.codecrafters.task.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiGenerateTaskResponse {
    private String title;
    private String taskType;
    private String description;
    private String instructions;
    private String inputData;
    private String expectedResult;
    private List<String> hints;
    private String difficulty;
    private String topic;
    private String aiProvider;
    private final Map<String, Object> additionalFields = new LinkedHashMap<>();

    public String title() { return title; }
    public String taskType() { return taskType; }
    public String description() { return description; }
    public String instructions() { return instructions; }
    public String inputData() { return inputData; }
    public String expectedResult() { return expectedResult; }
    public List<String> hints() { return hints; }
    public String difficulty() { return difficulty; }
    public String topic() { return topic; }
    public String aiProvider() { return aiProvider; }
    public Map<String, Object> additionalFields() { return additionalFields; }

    public void setTitle(String title) { this.title = title; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public void setDescription(String description) { this.description = description; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
    public void setHints(List<String> hints) { this.hints = hints; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }

    @JsonAnySetter
    public void setAdditionalField(String name, Object value) {
        additionalFields.put(name, value);
    }
}
