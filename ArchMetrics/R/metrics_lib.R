library(Hmisc)
library(xtable)
beamer.file<-file.path(getwd(),"beamer", "test.tex")


getMultiplier<-function(x){
  data<-NULL
  data <-read.csv(file="./metric_name.csv", header=T, sep=",")
  rownames(data)<-data$Key
  data$Key <-NULL
  if(is.na(data[x,4])){
    stop(paste(x, " Multiplier was not found in the table of metric names"))
  }
  
  return(data[x,4])
}
getDisplayName<-function(x){
  data<-NULL
  data <-read.csv(file="./metric_name.csv", header=T, sep=",")
  rownames(data)<-data$Key
  data$Key <-NULL
  if(is.na(data[x,1])){
     stop(paste(x, "was not found in the table of metric names"))
  }
  
  return(data[x,1])
  
}
getControlValue<-function(x){
  if(x != "System"){
    data<-NULL
    data <-read.csv(file="./metric_name.csv", header=T, sep=",")
    rownames(data)<-data$Key
    data$Key <-NULL
    if(is.na(data[x,3])){
      stop(paste(x, "control value was not found in the table of metric names"))
    }
    
    return(data[x,3])
  }
}

printLatexTable<- function(x, digits, caption.long, caption.short, label, file){
  #Set the significant digits
  xtmp <- xtable(x, digits=digits)
  # Set the caption
  caption(xtmp) <- c(caption.long, caption.short)
  # Set the label
  label(xtmp) <- label
  print(xtmp, include.rownames = T)
  # Other possible options: caption.placement="top", 
  print.xtable(xtmp, file=file, only.contents=TRUE)
}

applyMultiplier<-function(x, multiplier){
  print(str(x))
  x*multiplier
  
}
getMetric<-function(metricName, table, doMultiplier=FALSE){
  if(doMultiplier){
    multiplier<- getMultiplier(metricName)
    lapply(table[metricName,], function(x, m){ lapply(x,applyMultiplier, multiplier=m)}, m=multiplier)
  }else{
    table[metricName,]
  }

}

setDisplayNames<-function(merticNames, table){
  displayNames <- sapply(metricNames, getDisplayName)
  row.names(table)<- displayNames
  return (table)
}
getUnlistedMetric<-function(metricName, table){
  unlist(table[metricName,])
}

loadSys<- function(system){
  wd <- getwd() 
  csvPath <- file.path(wd,"csv")
  pattern <- paste("R__",system,"_All_Metrics",".csv", sep="")
  pathname <-file.path(csvPath, pattern)
  tryVal<-try(table <- read.csv(file=pathname, sep=',', header=T),silent=T)
  if (inherits(tryVal, "try-error"))
    print(paste(pattern, " was unsuccessfully read"))

  return(table)
  
}

loadSystemIndividual<-function(){
  MD<-loadSys("MD")
  CDB<-loadSys("CDB")
  AFS<-loadSys("AFS")
  DL<-loadSys("DL")
  PX<-loadSys("PX")
  JHD<-loadSys("JHD")
  HC<-loadSys("HC")
  APD<-loadSys("APD")
}
addEpsToBeamerFile<- function(metricName, epsFile, plotFunc=NULL, system=NULL, file=beamer.file){
  
  #TODO: Escape underscore for framename
  #TODO: Escape underscore for metricName
  
  appendedText<-paste("\\begin{frame}{",latexTranslate(paste(system, metricName, plotFunc, sep=" ")), "}
\\begin{figure}[!tb]
\\centering
\\includegraphics[width=0.8\\textwidth, clip, trim = 0pt 0pt 0pt 0pt]{./../eps/",epsFile,".eps}
\\caption{\\label{fig:",epsFile,"}",latexTranslate(metricName),"}
\\end{figure}
\\end{frame}
",sep="")
  write(x=appendedText, file=beamer.file, append=TRUE)
}

source(file="./metrics_plotting.R")

#Constants

systems <- c('MD', 'CDB', 'AFS', 'DL', 'PX', 'JHD', 'HC', 'APD')
table <- sapply(systems, loadSys)

checkTable<- function(){
  if(!is.matrix(table)){
    for(name in  names(table)){
      if(is.list(table[[name]])){
        
        print(paste(name, "[",length(table[[name]]), "]"))
      }
      
    }
    stop("table variable is not a matrix check files for inconsistencies in above list sizes")
  } 
}



##Metrics Summary Functions 

summarizeTable<-function(MetricName, table){
  if(MetricName != "System"){
    #compute the summary stats for metric mi
    list <- unlist(table[MetricName,])
    metric_min <- min(list, na.rm=T)
    metric_max <- max(list, na.rm=T)
    metric_sd  <- sd(list, na.rm=T)
    metric_avg <- mean(list, na.rm=T)
    
    v<-c(metric_min, metric_max,  metric_sd,  metric_avg)
    names(v) = c("Min", "Max", "Std Dev", "Mean")
    return(v)
  }else{
    return(0)
  }
}

getCompleteSummaryTable <- function(){
  checkTable()
  #create the summary list
  summary_table<-sapply(row.names(table),summarizeTable, table)
  #convert the list to a matrix
  summary_table<-do.call(rbind,summary_table)
  #merge the summary matrix with the system matrix
  final_table<-merge(table, summary_table, by = "row.names", all = TRUE)
  rownames(final_table)<- final_table[,1]
  final_table <- final_table[,-1]
  
  control_matrix <- matrix(sapply(row.names(final_table), getControlValue,USE.NAMES=T))
  rownames(control_matrix)<- rownames(final_table)
  colnames(control_matrix)<- c("Control")
  final_table<- merge(control_matrix, final_table, by="row.names", all=TRUE)
  rownames(final_table)<- final_table[,1]
  final_table <- final_table[,-1]
  return(final_table)
}

#Auto load summary table for usage
summary_table<-getCompleteSummaryTable()

getSubsetOfSummaryTable <- function(metricNames, doMultiplier=FALSE){
  resultTable<-sapply(metricNames, getMetric, summary_table, doMultiplier)
  resultTable<-t(resultTable)
  displayNames<-sapply(metricNames, getDisplayName)
  row.names(resultTable)<- displayNames
  return(resultTable)
}