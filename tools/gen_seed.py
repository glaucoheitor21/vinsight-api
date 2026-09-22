# -*- coding: utf-8 -*-
import random, io, datetime as dt

random.seed(20260922)
HOJE = dt.date(2026, 9, 22)
OUT = []
APOS = chr(39)


def w(s=""):
    OUT.append(s)


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, dt.datetime):
        return APOS + v.strftime("%Y-%m-%d %H:%M:%S") + APOS
    if isinstance(v, dt.date):
        return APOS + v.isoformat() + APOS
    return APOS + str(v).replace(APOS, APOS + APOS) + APOS


def insert(tabela, cols, linhas):
    w("INSERT INTO %s (%s) VALUES" % (tabela, ", ".join(cols)))
    for i, l in enumerate(linhas):
        fim = ";" if i == len(linhas) - 1 else ","
        w("    (%s)%s" % (", ".join(q(v) for v in l), fim))
    w()


HASHES = {
    "consultor123": "$2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a",
    "gerente123": "$2a$10$ktRfGhIFTnvbbzkGYCn/6eIoo9CoZKsuwO8B2cyQ/NDSugP/nO6kS",
    "analista123": "$2a$10$TVQxfAO9.N.UUpohYOvOlu.InbvAieI0MvqdN8Jgte6b6pEfDrbVO",
    "admin123": "$2a$10$DnAfMjLEifJRN3rZbEOGN.vYqRLSj4lUOMRqn3QEhU.KgcbzRSot2",
}

w("-- =============================================================================")
w("-- US-28 - Massa de demonstracao do VINSight Ford.")
w("--")
w("-- ARQUIVO GERADO. Aplicado SOMENTE no perfil dev: application-dev.properties")
w("-- inclui classpath:db/seed em spring.flyway.locations. O perfil prod nao o le.")
w("--")
w("-- Senhas: consultor123 / gerente123 / analista123 / admin123 (BCrypt).")
w("-- Data de referencia da massa: %s" % HOJE.isoformat())
w("-- =============================================================================")
w()

# ---------------------------------------------------------------- concessionarias
CONC = [
    (1, "Ford Morumbi", "SP-001", "Morumbi Veiculos Ltda", "11222333000181",
     "Av. Giovanni Gronchi", "5900", "Morumbi", "Sao Paulo", "SP", "05724003",
     "morumbi@ford.com.br", "1130115900"),
    (2, "Ford Campinas", "SP-014", "Campinas Motors S.A.", "22333444000172",
     "Av. John Boyd Dunlop", "1200", "Jardim Ipaussurama", "Campinas", "SP", "13060900",
     "campinas@ford.com.br", "1932281200"),
    (3, "Ford Porto Alegre", "RS-003", "Sul Automoveis Ltda", "33444555000163",
     "Av. Assis Brasil", "3400", "Sarandi", "Porto Alegre", "RS", "91010000",
     "poa@ford.com.br", "5133403400"),
]
insert("concessionarias",
       ["id", "nome_fantasia", "codigo", "razao_social", "cnpj", "logradouro", "numero",
        "complemento", "bairro", "cidade", "uf", "cep", "email", "telefone",
        "opt_in_whats_app", "ativo"],
       [(c[0], c[1], c[2], c[3], c[4], c[5], c[6], None, c[7], c[8], c[9], c[10], c[11],
         c[12], True, True) for c in CONC])

# ---------------------------------------------------------------- usuarios
USERS = [
    (1, "Ana Souza", "consultor@ford.com.br", "consultor123", "CONSULTOR", 1),
    (2, "Bruno Lima", "consultor2@ford.com.br", "consultor123", "CONSULTOR", 1),
    (3, "Carla Nunes", "consultor.campinas@ford.com.br", "consultor123", "CONSULTOR", 2),
    (4, "Diego Alves", "consultor.poa@ford.com.br", "consultor123", "CONSULTOR", 3),
    (5, "Eduardo Reis", "gerente@ford.com.br", "gerente123", "GERENTE", 1),
    (6, "Fernanda Dias", "gerente.campinas@ford.com.br", "gerente123", "GERENTE", 2),
    (7, "Gustavo Pinto", "analista@ford.com.br", "analista123", "ANALISTA_FORD", None),
    (8, "Helena Castro", "admin@ford.com.br", "admin123", "ADMIN", None),
]
insert("usuarios",
       ["id", "nome", "email", "senha", "perfil", "concessionaria_id", "ativo", "data_cadastro"],
       [(u[0], u[1], u[2], HASHES[u[3]], u[4], u[5], True, dt.datetime(2026, 1, 15, 9, 0, 0))
        for u in USERS])

# ---------------------------------------------------------------- clientes
NOMES = ["Carlos Pereira", "Mariana Rocha", "Joao Batista", "Patricia Gomes", "Rafael Antunes",
         "Luciana Mendes", "Paulo Cesar Silva", "Beatriz Carvalho", "Marcos Vinicius",
         "Juliana Prado", "Ricardo Tavares", "Camila Freitas", "Anderson Luz",
         "Tatiane Moreira", "Felipe Barros", "Vanessa Coelho", "Rodrigo Nogueira",
         "Simone Farias", "Leandro Pires", "Aline Cardoso", "Gabriel Fontes",
         "Renata Siqueira", "Thiago Ramos", "Priscila Amaral", "Vitor Hugo Braga",
         "Debora Lopes", "Alexandre Teles", "Natalia Guedes", "Sergio Monteiro",
         "Elaine Ribeiro"]
CIDADES = {1: ("Sao Paulo", "SP", "05724"), 2: ("Campinas", "SP", "13060"),
           3: ("Porto Alegre", "RS", "91010")}
CANAIS = ["WHATSAPP", "EMAIL", "TELEFONE"]

clientes, cli_conc = [], {}
for i, nome in enumerate(NOMES, start=1):
    conc = 1 if i <= 14 else (2 if i <= 23 else 3)
    cli_conc[i] = conc
    cidade, uf, cep5 = CIDADES[conc]
    cpf = "%011d" % (11122233300 + i * 137)
    nasc = dt.date(1965 + (i * 7) % 35, 1 + (i * 5) % 12, 1 + (i * 11) % 28)
    # 4 clientes sem consentimento: alimentam o cenario de supressao LGPD_OPT_OUT (US-35)
    consent = i not in (5, 12, 19, 27)
    canal = CANAIS[i % 3]
    canais = "WHATSAPP,EMAIL" if canal == "WHATSAPP" else canal
    primeiro = nome.split()[0].lower()
    clientes.append((
        i, nome, cpf, nasc,
        "Rua das Palmeiras", str(100 + i * 3), None, "Centro", cidade, uf,
        cep5 + "%03d" % (i * 7 % 1000),
        "%s.%s@email.com" % (primeiro, i), "119%08d" % (10000000 + i * 4321),
        canal == "WHATSAPP",
        dt.datetime(2021 + i % 5, 1 + i % 12, 1 + i % 28, 10, 0, 0), True,
        consent, canais if consent else None,
        dt.datetime(2026, 1 + i % 9, 1 + i % 28, 14, 22, 0), canal,
        None if i % 4 == 0 else 6 + (i % 5),
    ))
insert("clientes",
       ["id", "nome", "cpf", "data_nascimento", "logradouro", "numero", "complemento",
        "bairro", "cidade", "uf", "cep", "email", "telefone", "opt_in_whats_app",
        "data_cadastro", "ativo", "consentimento_ativo", "consentimento_canais",
        "consentimento_atualizado_em", "canal_preferido", "ultimo_nps"],
       clientes)

# ---------------------------------------------------------------- veiculos
MODELOS = [("Ranger", "XLT 3.2 Diesel", 40000), ("Ranger", "Limited 3.0 V6", 40000),
           ("Territory", "Titanium 1.5T", 20000), ("Bronco Sport", "Wildtrak 2.0", 20000),
           ("Maverick", "Lariat 2.0", 20000), ("Mustang Mach-E", "GT", 30000),
           ("Ka", "SE 1.0", 10000), ("EcoSport", "Freestyle 1.5", 15000)]
CORES = ["Prata", "Preto", "Branco", "Cinza", "Vermelho", "Azul"]
CONS = "BCDFGHJKLMNPRSTVWXYZ"
DIG = "0123456789"

veiculos, veic_dono, veic_conc, veic_modelo = [], {}, {}, {}
vid = 0
for cli in range(1, 31):
    qtd = 2 if cli % 4 == 0 else 1  # alguns clientes com 2 veiculos
    for _ in range(qtd):
        vid += 1
        conc = cli_conc[cli]
        modelo, versao, interv = MODELOS[vid % len(MODELOS)]
        ano_fab = 2018 + (vid % 8)
        vin = "9BF" + "".join(random.choice(CONS + DIG) for _ in range(13)) + "%d" % (vid % 10)
        placa = "%s%s%s%d%s%d%d" % (random.choice(CONS), random.choice(CONS),
                                    random.choice(CONS), vid % 10, random.choice(CONS),
                                    (vid * 3) % 10, (vid * 7) % 10)
        compra = dt.date(ano_fab, 1 + (vid * 5) % 12, 1 + (vid * 3) % 28)
        km = interv * (1 + vid % 4) + (vid * 137) % 9000
        garantia = compra + dt.timedelta(days=365 * 3)  # 3 anos de garantia de fabrica
        rev_km = interv * (1 + km // interv)
        # metade das proximas revisoes ja vencidas: alimenta o motivo de contato dos leads
        if vid % 2 == 0:
            rev_data = HOJE - dt.timedelta(days=(vid * 23) % 200 + 5)
        else:
            rev_data = HOJE + dt.timedelta(days=(vid * 17) % 180 + 5)
        tem_telemetria = vid % 3 != 0
        veic_dono[vid], veic_conc[vid], veic_modelo[vid] = cli, conc, (modelo, interv)
        if tem_telemetria:
            falhas = "P0420" if vid % 7 == 0 else ("P0301,P0171" if vid % 11 == 0 else None)
            recebida = dt.datetime(2026, 9, 14 + vid % 8, 22, 10, 0)
        else:
            falhas, recebida = None, None
        veiculos.append((
            vid, vin, placa, modelo, versao, ano_fab, ano_fab + 1, compra, "ATIVO", cli, conc,
            CORES[vid % len(CORES)], km, garantia, rev_km, rev_data, recebida, falhas,
        ))
insert("veiculos",
       ["id", "vin", "placa", "modelo", "versao", "ano_fabricacao", "ano_modelo",
        "data_compra", "status", "cliente_id", "concessionaria_compra_id", "cor",
        "quilometragem_estimada", "garantia_data_limite", "proxima_revisao_km",
        "proxima_revisao_data", "telemetria_recebida_em", "telemetria_codigos_falha"],
       veiculos)
TOTAL_VEIC = vid

# ---------------------------------------------------------------- ordens de servico
TIPOS = ["REVISAO_PROGRAMADA", "REPARO", "TROCA_OLEO", "GARANTIA", "RECALL"]
DESCR = {"REVISAO_PROGRAMADA": "Revisao de %d.000 km",
         "REPARO": "Reparo corretivo - suspensao dianteira",
         "TROCA_OLEO": "Troca de oleo e filtros",
         "GARANTIA": "Atendimento em garantia - modulo eletronico",
         "RECALL": "Campanha de recall - airbag"}
ordens, os_id = [], 0
for v in range(1, TOTAL_VEIC + 1):
    # fidelidade do veiculo a rede: e isso que faz o VIN Share variar entre unidades
    fidelidade = [0.95, 0.80, 0.55, 0.25][v % 4]
    for n in range(1, 4 + v % 3):
        na_rede = random.random() < fidelidade
        tipo = TIPOS[(v + n) % len(TIPOS)]
        data = HOJE - dt.timedelta(days=180 * n + (v * 13) % 90)
        if data.year < 2023:
            continue
        os_id += 1
        km_serv = max(5000, veiculos[v - 1][12] - 9000 * n)
        desc = DESCR[tipo] % (km_serv // 1000) if "%d" in DESCR[tipo] else DESCR[tipo]
        valor = round(380 + ((v * 97 + n * 53) % 1800) + (250 if tipo == "REPARO" else 0), 2)
        ordens.append((os_id, v, veic_conc[v] if na_rede else None, data, tipo, desc,
                       valor, km_serv, na_rede))
insert("ordens_servico",
       ["id", "veiculo_id", "concessionaria_id", "data_servico", "tipo_servico",
        "descricao", "valor", "quilometragem", "na_rede"],
       ordens)

# ---------------------------------------------------------------- agendamentos
agend = []
for i, v in enumerate(range(1, TOTAL_VEIC + 1, 2), start=1):
    if i > 20:
        break
    agend.append((i, v, veic_conc[v], dt.datetime(2026, 10, 1 + i % 28, 8 + i % 9, 0, 0),
                  ["REVISAO", "REPARO", "GARANTIA", "RECALL"][i % 4],
                  ["AGENDADO", "CONFIRMADO", "REALIZADO", "CANCELADO"][i % 4],
                  None, round(500 + (i * 137) % 1500, 2),
                  dt.datetime(2026, 9, 1 + i % 20, 11, 0, 0)))
insert("agendamentos",
       ["id", "veiculo_id", "concessionaria_id", "data_hora", "tipo_servico", "status",
        "observacoes", "valor_estimado", "data_criacao"],
       agend)

# ---------------------------------------------------------------- leads
PERFIS = ["FIEL", "ABANDONO", "ESQUECIDO", "ECONOMICO"]
ACOES = {
    "ESQUECIDO": "Oferecer pacote de revisao com desconto de fidelidade",
    "ABANDONO": "Ligar oferecendo test-drive do novo modelo e avaliacao do usado",
    "ECONOMICO": "Apresentar linha de pecas Motorcraft com preco competitivo",
    "FIEL": "Convidar para revisao antecipada com brinde de fidelidade",
}
# status/prioridade usam os enums ATUAIS do codigo v1. A US-35 migra para o
# vocabulario do contrato (OPEN/CONTATADO/AGENDADO/...) junto com a troca do enum.
STATUS_V1 = ["NOVO", "NOVO", "NOVO", "EM_CONTATO", "CONVERTIDO", "PERDIDO"]
leads = []
lid = 0
for v in range(1, TOTAL_VEIC + 1):
    if v % 8 == 0:
        continue
    lid += 1
    if lid > 50:
        break
    score = round(0.15 + ((v * 173) % 850) / 1000.0, 2)
    if score >= 0.85:
        prioridade = "CRITICA"
    elif score >= 0.70:
        prioridade = "ALTA"
    elif score >= 0.40:
        prioridade = "MEDIA"
    else:
        prioridade = "BAIXA"
    perfil = PERFIS[v % 4]
    rev_data = veiculos[v - 1][15]
    rev_km = veiculos[v - 1][14]
    if rev_data < HOJE:
        motivo = "Revisao de %d.000 km vencida ha %d dias" % (rev_km // 1000, (HOJE - rev_data).days)
    else:
        motivo = "Revisao de %d.000 km prevista para %s" % (rev_km // 1000, rev_data.isoformat())
    garantia = veiculos[v - 1][13]
    if HOJE < garantia <= HOJE + dt.timedelta(days=120):
        motivo += " e garantia encerra em %d meses" % max(1, (garantia - HOJE).days // 30)
    leads.append((lid, veic_dono[v], v, veic_conc[v], score, prioridade, STATUS_V1[v % 6],
                  motivo, dt.datetime(2026, 9, 15, 8, 0, 0), None,
                  motivo, ACOES[perfil], perfil, None))
insert("leads",
       ["id", "cliente_id", "veiculo_id", "concessionaria_id", "score", "prioridade",
        "status", "motivo", "data_geracao", "data_conversao", "motivo_contato",
        "acao_recomendada", "perfil_comportamental", "ultimo_contato_em"],
       leads)

w("-- Resumo: %d concessionarias, %d usuarios, %d clientes, %d veiculos, "
  "%d ordens de servico, %d agendamentos, %d leads."
  % (len(CONC), len(USERS), len(clientes), len(veiculos), len(ordens), len(agend), len(leads)))

dest = "C:/Users/labsfiap/Downloads/vinsight-api/src/main/resources/db/seed/V900__seed_demo_data.sql"
io.open(dest, "w", encoding="utf-8", newline="\n").write("\n".join(OUT) + "\n")

na_rede = sum(1 for o in ordens if o[8])
vins = set(o[1] for o in ordens)
vins_rede = set(o[1] for o in ordens if o[8])
print("gerado: %d linhas" % len(OUT))
print("concessionarias=%d usuarios=%d clientes=%d veiculos=%d" %
      (len(CONC), len(USERS), len(clientes), len(veiculos)))
print("ordens=%d (na_rede=%d) | share por ordem=%.2f | share por VIN=%.2f" %
      (len(ordens), na_rede, na_rede / len(ordens), len(vins_rede) / len(vins)))
print("agendamentos=%d leads=%d" % (len(agend), len(leads)))
print("vins unicos=%d placas unicas=%d" %
      (len(set(x[1] for x in veiculos)), len(set(x[2] for x in veiculos))))
print("cpfs unicos=%d" % len(set(c[2] for c in clientes)))
print("vin len ok=%s placa len ok=%s" %
      (all(len(x[1]) == 17 for x in veiculos), all(len(x[2]) == 7 for x in veiculos)))
