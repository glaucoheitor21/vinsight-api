package br.com.fiap.vinsight_api;

import br.com.fiap.vinsight_api.shared.FusoHorario;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class VinsightApiApplication {

	public static void main(String[] args) {
		// A aplicacao opera no horario de Brasilia (o da rede de concessionarias), qualquer que seja
		// o fuso da maquina. Os DATETIME do banco estao nesse fuso e o driver do MySQL converte para o
		// fuso da JVM: num servidor em UTC, sem esta linha, todo horario sairia 3 h adiantado.
		// Os testes fixam o mesmo fuso pelo argLine do surefire (pom.xml).
		TimeZone.setDefault(TimeZone.getTimeZone(FusoHorario.BRASILIA));
		SpringApplication.run(VinsightApiApplication.class, args);
	}

}
