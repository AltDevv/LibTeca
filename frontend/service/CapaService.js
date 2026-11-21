export function enquadramento(largura,altura,zoom=1,x=50,y=50,inteira=false) {
    const escala=(inteira?Math.min:Math.max)(600/largura,800/altura)*zoom;
    const w=largura*escala,h=altura*escala;
    return {x:(600-w)*x/100,y:(800-h)*y/100,w,h};
}
