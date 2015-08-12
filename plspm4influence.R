library("plspm")
#-------------------------------
md_outer_model <- read.delim("out0.txt")
md=cbind(md_outer_model)
#------------------------
# Model

nbLV=9
iniV=integer(nbLV)
# 
Profile=iniV
c0=c(18,19,31)
# 
Activity=iniV
c1=c(11,12,15,24,47)
# 
Connection=iniV
c2=c(22,23,25,27)
# 
Interaction=iniV
c3=c(3,4,5,6,7,8,9,10,13,14,26,43,46)
# 
Lexical=iniV
c4=c(40,41)
# 
Stylistic=iniV
c5=c(16,17,32,33,34,35,36,37,38,42,44,45,48,49,51)

Klout=iniV
c6=c(30)

Cosinus=c(1,1,0,0,1,1,0,0,0)
c7=c(52)

INFLUENCE=c(0,0,1,1,0,0,1,1,0)
c8=c(2)

md_path=rbind(Profile,Activity,Connection,Interaction,Lexical,Stylistic,Klout,Cosinus,INFLUENCE)
md_modes=c("A","A","A","A","A","A","A","B","B")
colnames(md_path)=rownames(md_path)
md_blocks=list(c0,c1,c2,c3,c4,c5,c6,c7,c8)

md_pls=plspm(md,md_path,md_blocks)
#md_pls=plspm(md,md_path,md_blocks,boot.val = TRUE, br = 200)

#plot(md_pls)
plot(md_pls,what="loadings")
