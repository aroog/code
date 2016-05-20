

#load custom library functions and data
source(file="./metrics_lib.R")

#Cluster metrics
metrics <-c('WA','WAR','WAWB','WAWBR','WAB','WABR','HMO','1FnE','HMN','TMO','TOS','1PnD')

#Cleanup the file
if(file.exists(beamer.file)){
  file.remove(beamer.file)
}

#Metrics across systems
acrossSystemsMetrics<- c('WA_Cluster_Mean','WAR_Cluster_Mean','WAWB_Cluster_Mean','WAWBR_Cluster_Mean','WAB_Cluster_Mean','WABR_Cluster_Mean','HMO_Cluster_Mean','X1FnE_N_Mean','HMN_Cluster_Mean','TMO_N_Mean','TOS_N_Mean','X1PnD_Cluster_Mean','X1DnP_Cluster_Mean','PO_P','InhD_P','InhE_P','SO_F_Median','SO_F_Mean', 'SO_F_Max', 'PTEP_F_Median','PTEP_F_Mean','PTEP_F_Max')

#Plot metrics for set sizes
plotfunctions<- c("hist", "qqnorm", "boxplot","barplot")
for(system in systems){
  for(metric in metrics){
    for(p_func in plotfunctions){
      plotMetric(system, metric, plot_fun=p_func)  
    }
    
  }
}

#Plotting metric across systems
sapply(acrossSystemsMetrics, plotAllSystem, table)

#Metric Names to find in table
listRelatedMetrics<- list(
                   c('TMO_N_Mean', 'TOS_N_Mean'),
                   c('X1DnP_Cluster_Mean','X1PnD_Cluster_Mean'),
                   c('X1DnP_Cluster_Max','X1PnD_Cluster_Max'),
                   c('InhE_P'),
                   c('PTEP_F_Max'),
                   c('WA_Cluster_Mean','WAR_Cluster_Mean','WAWB_Cluster_Mean','WAWBR_Cluster_Mean','WAB_Cluster_Mean','WABR_Cluster_Mean'),
                   c('WA_Cluster_Max','WAR_Cluster_Max','WAWB_Cluster_Max','WAWBR_Cluster_Max','WAB_Cluster_Max','WABR_Cluster_Max')
                   )
#Title for the related metrics 
listTitles<- list(
                  'TMO TOS Mean',
                  '1DnP 1PnD Mean',
                  '1DnP 1PnD Max',
                  "InhE_P", 
                  "PTEP_F_MAX", 
                  "WA_WAR_WAWB_WAWBR_WAB_WABR Mean",
                  "WA_WAR_WAWB_WAWBR_WAB_WABR Max")

for(i in 1:length(listRelatedMetrics)){
  plotRelatedMetrics(listRelatedMetrics[[i]], table, main=listTitles[[i]])
}

