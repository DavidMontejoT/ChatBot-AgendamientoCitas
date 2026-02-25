package com.chatbox.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageRequest {

    @NotBlank(message = "El número de teléfono es requerido")
    @Pattern(regexp = "^\\+\\d{1,15}$", message = "Formato de teléfono inválido. Use: +codigo_numero")
    private String from;

    @NotBlank(message = "El mensaje es requerido")
    private String message;
}
