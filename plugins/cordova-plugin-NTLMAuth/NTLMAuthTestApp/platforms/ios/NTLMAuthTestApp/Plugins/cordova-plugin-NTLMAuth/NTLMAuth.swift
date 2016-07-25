//
// NTLMAuth.swift
// NTLMAuth Cordova Plugin
//
// Created by Bao Thien Pham on 5/3/16.
// Copyright (c) 2016 Pfizer. All rights reserved.
//

import Foundation

@objc(NTLMAuth) class NTLMAuth : CDVPlugin {
    
    /**
    This method will be called from the Javascript file. This acts as a bridge from Cordova to Native iOS
    :param: command This is the Cordova object that can access Javascript data
    :returns: void
    */
    func callNtlmMethods(command: CDVInvokedUrlCommand){
        var pluginResult    = CDVPluginResult(status: CDVCommandStatus_ERROR)
        
        let networkManager  = NetworkManager()
        let ntlmMethod      = NtlmMethod()
        
        let username        = command.arguments[0] as! String
        let password        = command.arguments[1] as! String
        let url             = command.arguments[2] as! String               //"http://events-dev.pfizer.com/event/1"
        let urlPath         = command.arguments[3] as! String
        let methodName      = command.arguments[4] as! String
        let header          = command.arguments[5] as! String
        let data            = command.arguments[6] as! String
        
        
        
        if username.characters.count > 0 &&
           password.characters.count > 0{
            
            //create Credentials and prepare for authentication
            networkManager.createCredentialWithUserName(username, password: password)
            
            //convert headerinto dictionaries
            let headerConverted = convertToDictionary(header)
            
            if url.characters.count > 0{
                
                if methodName.characters.count > 0{
                    
                    switch methodName {
                    case ntlmMethod.HTTP_GET:
                        networkManager.NtlmGET(url, path: urlPath, headers: headerConverted,
                                            success: {data in
                                                print("Data: \(data)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Successful Making GET Request")
                                                //return the result to Javascript (invoke the callback)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return data
                            },
                                            failure: {error in
                                                print("Error: \(error)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: error.domain)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return error
                        })
                    case ntlmMethod.HTTP_POST:
                        let dataConverted   = convertToDictionary(data)
                        if dataConverted == [:] {
                            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "No data to POST")
                            self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                        }else{
                            networkManager.NtlmPOST(url, path: urlPath, headers: headerConverted, data: dataConverted,
                                             success: {data in
                                                print("Data: \(data)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Successful Making POST Request")
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return data
                                },
                                             failure: {error in
                                                print("Error: \(error)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: error.domain)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return error
                            })
                        }
                    case ntlmMethod.HTTP_PUT:
                        let dataConverted   = convertToDictionary(data)
                        if dataConverted == [:] {
                            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "No data to POST")
                            self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                        }else{
                            networkManager.NtlmPUT(url, path: urlPath, headers: headerConverted, data: dataConverted,
                                            success: {data in
                                                print("Data: \(data)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Successful Making PUT Request")
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return data
                                },
                                            failure: {error in
                                                print("Error: \(error)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: error.domain)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return error
                            })
                        }
                    case ntlmMethod.HTTP_HEAD:
                        networkManager.NtlmHEAD(url, path: urlPath, headers: headerConverted,
                                             success: {data in
                                                print("Data: \(data)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Successful Making HEAD Request")
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return data
                            },
                                             failure: {error in
                                                print("Error: \(error)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: error.domain)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return error
                        })
                    case ntlmMethod.HTTP_DELETE:
                        networkManager.NtlmDELETE(url, path: urlPath, headers: headerConverted,
                                               success: {data in
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "Successful Making DELETE Request")
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return data
                            },
                                               failure: {error in
                                                print("Error: \(error)")
                                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: error.domain)
                                                self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                                                return error
                        })
                    default:
                        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Method Name Does Not Match")
                        self.commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                    }
                }else{
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "Method Name if empty")
                    commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
                }
            }else{
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "URL if empty")
                commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
            }
        }else{
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAsString: "User Name and/or Password is empty")
            commandDelegate.sendPluginResult(pluginResult, callbackId: command.callbackId)
        }
    }
    
    /**
    Converts Headers and Data input into Dictionary
    :param: input String input from the user
    :returns: Dictionary<String, String> 
    */
    private func convertToDictionary(input: String) -> Dictionary<String, String>{
        if input == "" {return [:]}
        
        var result: Dictionary<String, String> = [:]
        let inputArray = input.componentsSeparatedByString(",")
        
        for eachInput:String in inputArray{
            let keysAndValues = eachInput.componentsSeparatedByString(":")
            result[keysAndValues[0]] = keysAndValues[1]
        }
        return result
    }
}