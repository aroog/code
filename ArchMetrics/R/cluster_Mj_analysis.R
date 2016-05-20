library(orddom) # for cliff's delata and CI 
library(pastecs) #for descriptive stats
options(scipen=100)
options(digits=2)

#To draw plots to a postscript file add this line at the beginning of your program
#for inclusion in Computer Modern TeX documents, perhaps
# postscript("eps/waiwb.ps", width = 5.0, height = 5.0,
# horizontal = FALSE, onefile = FALSE, paper = "special",
# family = "Helvetica", encoding = "TeXtext.enc")

# !!!! preprocessing of data is needed - avoid empty cells. 
#to read data
#read all files given a pattern. we should probably put all files in a csv folder
files <- dir(path=".",pattern='*.csv')
for (f in files){
  Mj <- read.csv(file=f,sep=',',header=T)
  
  #summary stats
  #summary(Mj)
  
  testResult<-wilcox.test(Mj$ClusterSize, alternative="greater")
  print(testResult$p.value)
  
  # Use the same filename, remove the extension, and append ".txt"
  fname = paste(sub("^([^.]*).*","\\1",basename(f)),".txt", sep="")
  
  #save the result to file
  #TODO: add some description to the file contents
  write.table(testResult$p.value, file=fname, col.names = TRUE, row.names= FALSE)
  
  #Cliff's delta calculations
  #orddom(Mj$Control,Mj$ClusterSize,paired=FALSE,outputfile="results.txt")
}  
