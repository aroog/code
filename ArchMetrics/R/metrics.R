DIGITS = 2

source(file="./metrics_lib.R")
require(orddom)
library(orddom)

#TODO: Convert digits to constant
formatP<-function(x, digits=2){
  x<-formatC(x, digits=digits,format="f")
  if(x<0.05){
    paste("\\textbf{", x, "}",sep="")
  } else{
    x
  }

}

#TODO: Convert digits to constant
formatD<- function(d, p.val, digits=2){
  d<-formatC(d, digits=digits,format="f")
  if(p.val < 0.05){
    if(d>=0.333 && d<1){
      return(paste("\\textit{", d, "}", sep=""))
    }else{
      return(d)
    }
    
  }
  NA
}

# returns an array of p values as returned by wilcox.test
  runWilcox <- function(system, metric, path) {
    
    #Assume the CSV file is called System_Metric.csv
    pattern = paste("R__", system, "_", metric, ".csv", sep="")
    
    Mj <- NULL
    
    pathname <- file.path(path, pattern) 
    MjTable <- read.csv(pathname, sep=',', header=T)
    
    #If the metrics has NO data
    #If NO data, do not compute p, delta or D
    if(nrow(MjTable) == 0){
      pattern = ""
      summary(MjTable_Empty$ClusterSize)       
      Mj <- data.frame(p.value= NaN , Delta = NaN, DeltaMetric = NaN, stringsAsFactors=F)
    }
    #If the metrics has data
    if(nrow(MjTable) > 0)
    {
      sumResult <- summary(MjTable$ClusterSize)
      testResult <- tryCatch( wilcox.test(MjTable$ClusterSize, alternative="greater"), error = function(e) print(pattern) )
      controlVal <- getControlValue(x=metric)
      controlVector <- rep(controlVal, length(MjTable$ClusterSize))
      
      # Same for Cliff's Delta calculation    
      
      # Wrap oorddom computation with try to avoid error about: "essentially  constant value"
      # use first column in the result to allow interpretation small-medium-large difference. The second column gives us a raw number 
      ord <- try(orddom(controlVector, MjTable$ClusterSize, paired=FALSE), silent=T)
      # Take out Cohen's d, since the test is non-parametric
      # if (is(ord, "try-error")) cohenDOrd <- NA else cohenDOrd <- ord["Cohen's d", 1]
      #if (is(ord, "try-error")) cohenDMetric <- NA else cohenDMetric <- ord["Cohen's d", 2]
      if (is(ord, "try-error")) cliffDeltaOrd <- NA else cliffDeltaOrd <- ord["delta", 1]
      if (is(ord, "try-error")) cliffDeltaMetric <- NA else cliffDeltaMetric <- ord["delta", 2]      
      
      Mj <- data.frame(p.value= formatP(testResult$p.value) , Delta = formatD(as.numeric(cliffDeltaOrd),testResult$p.value ), DeltaMetric = as.numeric(cliffDeltaMetric), stringsAsFactors=F)
      
    }    
    dput(Mj)    
    return(Mj)
  }

runMetric <- function (metric, systems, csvPath) {
  ret <- sapply(systems, runWilcox, metric, csvPath, simplify=T)
  return (ret)
}



# Could have an extra level of indirection; map from name in table vs. name of files.
# CDB -> CryptoDB
# TODO: HIGH. Pass the system name to the pattern, to make sure we are reading the correct CSV file for the system

#PX and HC takes a lot of time

#Systems to evaluate DDM
#systems <- c('Ermete SMS','Muspy')

#All the 8 subject systems - Training set
systems <- c('MD','CDB', 'AFS', 'DL', 'PX', 'JHD', 'HC', 'APD')
#DONE. Add: X1FnE.
#TODO: Re-order list of metrics.

metricNames <-c('WA', 'WAR', 'WAB','WABR', 'WAWB','WAWBR', 'HMO', '1FnE',  'HMN',  'TMO', 'TOS', '1PnD', '1DnP', 'PTEP', 'SO', 'DFEP', '1MInE' ,'1MInE_RecType' ,'1MInE_ArgType' ,'1MInE_RetType' ,'1FRnE', '1FRnE_RecType', '1FWnE', '1FWnE_RecType')
wd <- getwd() 
csvPath <- file.path(wd,"csv") 
  
listresult <- sapply(metricNames, runMetric, systems, csvPath)
rownames(listresult)<-rep(c("P.Value", "d", "D"), length(systems))
  
#transpose to have systems as columns and metrics as rows
listresult <- t(listresult)
#colnames(listresult)<-rep(c("Max.", "Median", "P.Value"), length(systems))
# Pick shorter names for the columns, e.g., use "M" for Mean
# Take out Cohen's "d", "d-size", 
# colnames(listresult)<-rep(c("p", "d", "d-size", "D", "D-size"), length(systems))
colnames(listresult)<-rep(c("p", "D", "D-size"), length(systems))
  


  
library(xtable)
#library(Hmisc)
#latex(latexTabular(result), file="./_metrics.tex", colheads=systems)
xtmp <- xtable(listresult, sanitize.text.function = function(x){x})
  
#Set the significant digits
#TODO: Check the digits for columns (in order): p, D, D-size
#XXX. Should not be hard-coded. Read from metric_name.csv file
#digits(xtmp) <- c(0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0 ,0,0,0 ,0,0,0)

#print(xtmp, include.rownames = TRUE)
# Other possible options: caption.placement="top", 

#Table for all 8 systems - Training set
print.xtable(xtmp, digits=DIGITS, file="./metrics.tex", only.contents=T, sanitize.text.function=function(x){x})

#Table for other systems - DDM
#print.xtable(xtmp, digits=DIGITS, file="./metrics_MuspyESMS.tex", only.contents=T, sanitize.text.function=function(x){x})

