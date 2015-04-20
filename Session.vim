let SessionLoad = 1
if &cp | set nocp | endif
let s:so_save = &so | let s:siso_save = &siso | set so=0 siso=0
let v:this_session=expand("<sfile>:p")
silent only
cd ~/Desktop/CypressWeb
if expand('%') == '' && !&modified && line('$') <= 1 && getline(1) == ''
  let s:wipebuf = bufnr('%')
endif
set shortmess=aoO
badd +0 build.sbt
badd +162 io/target/scala-2.11/src_managed/main/sbt-scalaxb/generated/xmlprotocol.scala
badd +41 io/target/scala-2.11/src_managed/main/sbt-scalaxb/generated/fedd_types_type1.scala
badd +0 io/src/main/wsdl/fedd.wsdl
badd +0 io/src/test/scala/fedd/FeddQSpec.scala
badd +0 project/plugins.sbt
badd +0 io/src/main/scala/fedd/FeddQ.scala
argglobal
silent! argdel *
edit io/src/main/wsdl/fedd.wsdl
set splitbelow splitright
wincmd _ | wincmd |
vsplit
1wincmd h
wincmd _ | wincmd |
split
1wincmd k
wincmd _ | wincmd |
vsplit
1wincmd h
wincmd w
wincmd w
wincmd w
wincmd _ | wincmd |
split
wincmd _ | wincmd |
split
2wincmd k
wincmd w
wincmd w
set nosplitbelow
set nosplitright
wincmd t
set winheight=1 winwidth=1
exe '1resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 1resize ' . ((&columns * 96 + 144) / 289)
exe '2resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 2resize ' . ((&columns * 96 + 144) / 289)
exe '3resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 3resize ' . ((&columns * 193 + 144) / 289)
exe '4resize ' . ((&lines * 38 + 58) / 117)
exe 'vert 4resize ' . ((&columns * 95 + 144) / 289)
exe '5resize ' . ((&lines * 38 + 58) / 117)
exe 'vert 5resize ' . ((&columns * 95 + 144) / 289)
exe '6resize ' . ((&lines * 37 + 58) / 117)
exe 'vert 6resize ' . ((&columns * 95 + 144) / 289)
argglobal
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 36 - ((35 * winheight(0) + 28) / 57)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
36
normal! 0
lcd ~/Desktop/CypressWeb
wincmd w
argglobal
edit ~/Desktop/CypressWeb/io/src/main/scala/fedd/FeddQ.scala
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 196 - ((28 * winheight(0) + 28) / 57)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
196
normal! 049|
lcd ~/Desktop/CypressWeb
wincmd w
argglobal
edit ~/Desktop/CypressWeb/io/target/scala-2.11/src_managed/main/sbt-scalaxb/generated/xmlprotocol.scala
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 2055 - ((56 * winheight(0) + 28) / 57)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
2055
normal! 0
lcd ~/Desktop/CypressWeb
wincmd w
argglobal
edit ~/Desktop/CypressWeb/project/plugins.sbt
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 29 - ((28 * winheight(0) + 19) / 38)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
29
normal! 0
lcd ~/Desktop/CypressWeb
wincmd w
argglobal
edit ~/Desktop/CypressWeb/io/src/test/scala/fedd/FeddQSpec.scala
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 35 - ((2 * winheight(0) + 19) / 38)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
35
normal! 037|
lcd ~/Desktop/CypressWeb
wincmd w
argglobal
edit ~/Desktop/CypressWeb/build.sbt
setlocal fdm=manual
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=0
setlocal fml=1
setlocal fdn=20
setlocal fen
silent! normal! zE
let s:l = 51 - ((10 * winheight(0) + 18) / 37)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
51
normal! 041|
lcd ~/Desktop/CypressWeb
wincmd w
3wincmd w
exe '1resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 1resize ' . ((&columns * 96 + 144) / 289)
exe '2resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 2resize ' . ((&columns * 96 + 144) / 289)
exe '3resize ' . ((&lines * 57 + 58) / 117)
exe 'vert 3resize ' . ((&columns * 193 + 144) / 289)
exe '4resize ' . ((&lines * 38 + 58) / 117)
exe 'vert 4resize ' . ((&columns * 95 + 144) / 289)
exe '5resize ' . ((&lines * 38 + 58) / 117)
exe 'vert 5resize ' . ((&columns * 95 + 144) / 289)
exe '6resize ' . ((&lines * 37 + 58) / 117)
exe 'vert 6resize ' . ((&columns * 95 + 144) / 289)
tabnext 1
if exists('s:wipebuf')
  silent exe 'bwipe ' . s:wipebuf
endif
unlet! s:wipebuf
set winheight=1 winwidth=20 shortmess=filnxtToO
let s:sx = expand("<sfile>:p:r")."x.vim"
if file_readable(s:sx)
  exe "source " . fnameescape(s:sx)
endif
let &so = s:so_save | let &siso = s:siso_save
doautoall SessionLoadPost
unlet SessionLoad
" vim: set ft=vim :
