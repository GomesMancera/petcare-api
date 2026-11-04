package br.com.petcare.api.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoVacinaTest {

    @Test
    void v10EV8TemReforcoEm21Dias() {
        assertThat(TipoVacina.intervaloEmDiasPara("V10")).isEqualTo(21);
        assertThat(TipoVacina.intervaloEmDiasPara("v8")).isEqualTo(21);
    }

    @Test
    void ignoraAcentosEspacosEMaiusculas() {
        assertThat(TipoVacina.intervaloEmDiasPara("  Antirrábica ")).isEqualTo(365);
        assertThat(TipoVacina.intervaloEmDiasPara("GIARDIA")).isEqualTo(365);
    }

    @Test
    void vacinaDesconhecidaUsaReforcoAnual() {
        assertThat(TipoVacina.intervaloEmDiasPara("Vacina inventada"))
                .isEqualTo(TipoVacina.INTERVALO_PADRAO_DIAS);
    }
}
