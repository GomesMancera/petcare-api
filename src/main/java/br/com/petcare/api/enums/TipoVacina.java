package br.com.petcare.api.enums;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Vacinas conhecidas e o intervalo (em dias) até a próxima dose.
 * Vacinas fora da lista usam o reforço anual padrão.
 */
public enum TipoVacina {
    V10("V10", 21),
    V8("V8", 21),
    ANTIRRABICA("Antirrábica", 365),
    GIARDIA("Giárdia", 365),
    GRIPE_CANINA("Gripe canina", 365);

    public static final int INTERVALO_PADRAO_DIAS = 365;

    private final String nome;
    private final int intervaloDias;

    TipoVacina(String nome, int intervaloDias) {
        this.nome = nome;
        this.intervaloDias = intervaloDias;
    }

    public String getNome() {
        return nome;
    }

    public int getIntervaloDias() {
        return intervaloDias;
    }

    /** Nomes para sugestão nos formulários. */
    public static List<String> nomes() {
        return Arrays.stream(values()).map(TipoVacina::getNome).toList();
    }

    /** Ignora maiúsculas, espaços nas pontas e acentos ("Antirrábica" == "antirrabica"). */
    public static int intervaloEmDiasPara(String nomeInformado) {
        String chave = normalizar(nomeInformado);
        return Arrays.stream(values())
                .filter(tipo -> normalizar(tipo.nome).equals(chave))
                .findFirst()
                .map(TipoVacina::getIntervaloDias)
                .orElse(INTERVALO_PADRAO_DIAS);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
