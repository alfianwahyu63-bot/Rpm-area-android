/* RPM Area V42 — pure WhatsApp template and store-alias helpers.
   No external requests and no mutation of master PLU, target or sales data. */
(function(root, factory){
  const api=factory();
  if(typeof module==='object'&&module.exports)module.exports=api;
  if(root)root.AreaWATools=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
'use strict';
function norm(value){
  return String(value??'').normalize('NFKD').replace(/[\u0300-\u036f]/g,'')
    .toUpperCase().replace(/[^A-Z0-9]+/g,' ').replace(/\s+/g,' ').trim();
}
function initials(store){
  const name=String(store?.name||'');
  const fromField=String(store?.initials||store?.initial||'').trim();
  if(fromField)return fromField.toUpperCase();
  const found=[...name.matchAll(/\[\s*([A-Z0-9]{2,12})\s*\]/gi)];
  return found.length?found[found.length-1][1].toUpperCase():'';
}
function shortName(name){
  let s=String(name||'').replace(/\[[^\]]+\]/g,' ').replace(/\([^)]*\)/g,' ');
  let n=norm(s);
  // Prefix and suffix used in imported store names; strip only full words.
  n=n.replace(/^(?:(?:NAMA\s+)?TOKO\s+|STORE\s+|ALFAMIDI\s+|ALFAMART\s+|SUPER\s+)+/g,'');
  n=n.replace(/\s+(?:FRANCHISE|REGULER|REGULAR)$/g,'').trim();
  return n;
}
function aliases(store){
  const output=new Set();
  const add=x=>{const v=norm(x);if(v)output.add(v)};
  add(store?.code);
  add(store?.name);
  add(initials(store));
  add(String(store?.name||'').replace(/\[[^\]]+\]/g,' '));
  add(shortName(store?.name));
  for(const a of Array.isArray(store?.aliases)?store.aliases:[])add(a);
  return [...output];
}
function resolveStore(raw, stores){
  const wanted=norm(shortName(raw));
  if(!wanted)return {status:'empty',matches:[]};
  const list=(stores||[]).filter(s=>s&&s.code);
  // Code takes precedence; never guess on similar store names.
  const exactCode=list.filter(s=>norm(s.code)===wanted);
  if(exactCode.length===1)return {status:'ok',store:exactCode[0],matches:exactCode,match:'code'};
  if(exactCode.length>1)return {status:'ambiguous',matches:exactCode};
  const initialMatches=list.filter(s=>norm(initials(s))===wanted);
  if(initialMatches.length===1)return {status:'ok',store:initialMatches[0],matches:initialMatches,match:'initial'};
  if(initialMatches.length>1)return {status:'ambiguous',matches:initialMatches};
  const candidates=list.filter(s=>aliases(s).includes(wanted));
  if(candidates.length===1)return {status:'ok',store:candidates[0],matches:candidates,match:'name'};
  if(candidates.length>1)return {status:'ambiguous',matches:candidates};
  return {status:'not_found',matches:[]};
}
function brandKey(brand){return norm(brand).replace(/\s+/g,'')}
function brandsOf(store){
  const known=new Map();
  for(const item of store?.items||[]){const name=String(item.brand||'LAINNYA').trim()||'LAINNYA';
   const key=brandKey(name);if(!known.has(key))known.set(key,name.toUpperCase());}
  return [...known.values()].sort((a,b)=>a.localeCompare(b,'id',{sensitivity:'base'}));
}
function makeTemplate(opts){
  const store=opts?.store;
  if(!store?.code)throw Error('Pilih toko dari hasil import Excel terlebih dahulu.');
  const items=Array.isArray(store.items)?store.items:[];
  if(!items.length)throw Error('List PLU toko ini masih kosong. Import file Excel LIST ITEM dahulu.');
  const selected=new Set((opts.brands||[]).map(brandKey));
  if(!selected.size)throw Error('Pilih minimal satu brand.');
  const included=items.filter(i=>selected.has(brandKey(i.brand||'LAINNYA'))&&String(i.plu||'').trim());
  if(!included.length)throw Error('Tidak ditemukan PLU untuk brand pilihan.');
  included.sort((a,b)=>{
   const na=String(a.brand||'LAINNYA').toUpperCase(),nb=String(b.brand||'LAINNYA').toUpperCase();
   const bc=na.localeCompare(nb,'id',{sensitivity:'base'});
   return bc||String(a.plu).localeCompare(String(b.plu),'id',{numeric:true});
  });
  let label='Kode Toko: '+store.code;
  if(opts.storeMode==='initial'&&initials(store))label='Inisial Toko: '+initials(store);
  if(opts.storeMode==='name')label='Nama Toko: '+shortName(store.name||store.code);
  const lines=['Format RPM per PLU','Periode: '+String(opts.period||'').trim(),label,''];
  let last='';
  for(const item of included){
   const brand=String(item.brand||'LAINNYA').trim().toUpperCase();
   if(brand!==last){if(last)lines.push('');lines.push('✅*'+brand+'*');last=brand;}
   lines.push(String(item.plu).trim()+'/');
  }
  return {text:lines.join('\n'),itemCount:included.length,brandCount:new Set(included.map(i=>brandKey(i.brand||'LAINNYA'))).size,storeCode:store.code};
}
return Object.freeze({norm,initials,shortName,aliases,resolveStore,brandsOf,brandKey,makeTemplate});
});
