export const nomesPrazos={UMA_SEMANA:'1 semana',UM_MES:'1 mês',DOIS_MESES:'2 meses'};
export function prazoTela(){
    return '<label>Tempo de empréstimo<select name="prazo" required><option value="">Selecione o prazo…</option>'+Object.entries(nomesPrazos).map(([valor,nome])=>'<option value="'+valor+'">'+nome+'</option>').join('')+'</select></label>';
}
