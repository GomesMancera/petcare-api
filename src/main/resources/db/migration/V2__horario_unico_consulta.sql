-- V2: impede, no próprio banco, duas consultas ativas no mesmo horário.
-- Garante a regra mesmo com requisições simultâneas (a checagem na aplicação sozinha tem condição de corrida).

-- 1) Higieniza dados de teste: se existir mais de uma consulta ativa no mesmo horário,
--    mantém a mais antiga (menor ID) e cancela as demais.
UPDATE PC_CONSULTA c
   SET c.STATUS = 'CANCELADA'
 WHERE c.STATUS <> 'CANCELADA'
   AND EXISTS (SELECT 1
                 FROM PC_CONSULTA o
                WHERE o.DATA_HORA = c.DATA_HORA
                  AND o.STATUS <> 'CANCELADA'
                  AND o.ID < c.ID);

-- 2) Índice único parcial: só consultas NÃO canceladas ocupam o horário
--    (CASE devolve NULL para canceladas, e NULL não entra no índice único).
CREATE UNIQUE INDEX UX_PC_CONSULTA_HORARIO
    ON PC_CONSULTA (CASE WHEN STATUS <> 'CANCELADA' THEN DATA_HORA END);
