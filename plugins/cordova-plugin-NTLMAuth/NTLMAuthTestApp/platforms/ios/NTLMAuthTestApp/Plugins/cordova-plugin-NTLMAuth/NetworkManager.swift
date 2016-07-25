//
//  NetworkManager.swift
//  NTLMAuth Cordova Plugin
//
//  Created by Bao Thien Pham on 5/3/16.
//  Copyright © 2016 Bao Thien Pham. All rights reserved.
//

import Foundation

class NetworkManager: NSObject, NSURLSessionDataDelegate{
    
    var ntlmMethod  = NtlmMethod()
    let sharedProtectionSpace = NSURLProtectionSpace(host: "pfizer.com", port: 0, protocol: "ntlm", realm: nil, authenticationMethod: NSURLAuthenticationMethodNTLM)
    var currentCredential: NSURLCredential?
    
    /**
     Send and execute the request
     :param: request: NSMutableURLRequest   A request for the server
             success(NSDictionary)          A callback when the task is successfully completed
             failure(NSError)               A callback when the task is failed
     :returns: Void
     */
    private func retrieveData(request: NSMutableURLRequest, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let defaultSession = NSURLSession(configuration: NSURLSessionConfiguration.defaultSessionConfiguration(), delegate: self, delegateQueue: nil)
        
        _ = defaultSession.dataTaskWithRequest(request){
            (data, response, error) in
            guard let dataRetrieved = data else {
                print("Error: did not receive data")
                failure(NSError(domain: "unauthorized", code: 401, userInfo: nil))
                return
            }
            guard let responseRetrieved = response else{
                print("Error: did not recieve response")
                return
            }
            guard error == nil else {
                print("Error: \(error!.description)")
                print(error)
                return
            }
            let statusCode = (response as! NSHTTPURLResponse).statusCode
            if request.HTTPMethod == self.ntlmMethod.HTTP_HEAD{
                success((responseRetrieved as! NSHTTPURLResponse).allHeaderFields.description)
                return
            }
            if statusCode < 300{
                // Convert server json response to NSDictionary
                do {
                    if let convertedJsonIntoDict = try NSJSONSerialization.JSONObjectWithData(dataRetrieved, options: .AllowFragments) as? [String : AnyObject] {
                        success(convertedJsonIntoDict.description)
                    }
                } catch let error as NSError {
                    failure(error)
                }
            }else{
                failure(NSError(domain: NSHTTPURLResponse.localizedStringForStatusCode(statusCode), code: statusCode, userInfo: nil))
            }   
        }.resume()
    }
    
    
    /**
     Prepare request
     :param: url: NSURL                             URL
             method: String                         HTTP Methods
             headers: Dictionary<String, String>    Headers for request
             data: NSDictionary                     Only required for PUT and POST method
             success(NSDictionary)                  A callback when the task is successfully completed
             failure(NSError)                       A callback when the task is failed
     :returns: Void
     */
    internal func action(url: NSURL, methodName: String, headers: Dictionary<String, String>?, data: NSDictionary?, success: (String) -> (String), failure: (NSError)-> (NSError)){
        
        let request = NSMutableURLRequest(URL: url)
       
        request.HTTPMethod = methodName
        
        //default headers to get json 
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.addValue("application/json", forHTTPHeaderField: "Content-type")
        
        //set headers if any
        if headers != nil{
            for (headerKey, headerValue) in headers! {
                request.setValue(headerValue, forHTTPHeaderField: headerKey)
            }
        }
        
        switch (methodName){
        case ntlmMethod.HTTP_GET:
            retrieveData(request, success: success, failure: failure)
        case ntlmMethod.HTTP_POST:
            do{
                request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(data!, options: [])
                retrieveData(request, success: success, failure: failure)
            }catch {
                print("Error: cannot create JSON from data")
            }
        case ntlmMethod.HTTP_PUT:
            do{
                request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(data!, options: [])
                retrieveData(request, success: success, failure: failure)
            }catch {
                print("Error: cannot create JSON from data")
            }
        case ntlmMethod.HTTP_HEAD:
            retrieveData(request, success: success, failure: failure)
        case ntlmMethod.HTTP_DELETE:
            retrieveData(request, success: success, failure: failure)
        default:
            print("Error: methodName does not match")
        }
    }
    
    
    /**
    Creates credential from given username and password and stores in keychain 
    :param: userName: String
            password: String
    :returns: NSURLCredential
    */
    func createCredentialWithUserName(userName: String, password: String) -> NSURLCredential{
        let credential = NSURLCredential(user: userName, password: password, persistence: .Permanent) //store in the key chain
        
        NSURLCredentialStorage.sharedCredentialStorage().setDefaultCredential(credential, forProtectionSpace: sharedProtectionSpace)
        
        currentCredential = credential
        
        return credential
    }
    
    /**
    Creates credential from keychain
    :param: userName: String
            password: String
    :returns: NSURLCredential
    */
    func createCredentialFromKeychain() -> NSURLCredential? {
        var result: NSURLCredential? = nil
        
        let username = KeychainWrapper.stringForKey(kSecAttrAccount as String) ?? ""
        let password = KeychainWrapper.stringForKey(kSecValueData as String) ?? ""
        
        if (username.characters.count > 0 && password.characters.count > 0) {
            
            result = createCredentialWithUserName(username, password: password)
        }
        return result
    }
    
    /**
    Handles authentication in task-level. Please refer to the Apple Documentation for details
    :param: session: NSURLSession
            task: NSURLSessionTask
            challenge: NSURLAuthenticationChallenge
            completionHandler (NSURLSessionAuthChallengeDisposition, NSURLCredential)
    :returns: Void
    */
    func URLSession(session: NSURLSession, task: NSURLSessionTask, didReceiveChallenge challenge: NSURLAuthenticationChallenge, completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Void) {
        
        if challenge.previousFailureCount > 2 {
            print("Authentication failed, \(challenge.failureResponse)")
            completionHandler(.CancelAuthenticationChallenge, nil)
            task.cancel()
            return
        }      
        if self.currentCredential != nil {
            print("Sending Authentication")
            completionHandler(.UseCredential, self.currentCredential!)
            return
        }else{
            if let credentials = createCredentialFromKeychain() {
                completionHandler(.UseCredential, credentials)
            }
            else{
                print("No Credential Found!")
                return
            }
        }
    }
    
    func URLSession(session: NSURLSession, didReceiveChallenge challenge: NSURLAuthenticationChallenge, completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Void) {

        if challenge.previousFailureCount > 2 {
            print("Authentication failed, \(challenge.failureResponse)")
            completionHandler(.CancelAuthenticationChallenge, nil)
            return
        }
        if self.currentCredential != nil {
            print("Sending Authentication")
            completionHandler(.UseCredential, self.currentCredential!)
            return
        }else{
            if let credentials = createCredentialFromKeychain() {
                completionHandler(.UseCredential, credentials)
            }
            else{
                
                print("No Credential Found!")
                return
            }
        }
    }

    /**
    GET Methods
    :param: url: String                         URL
            path: String                        Parameters for url 
            header: Dictionary<String, String>  Headers request. This can be nil
            success(NSDictionary)               A callback when the task is successfully completed
            failure(NSError)                    A callback when the task is failed
    :returns: Void
    */
    func NtlmGET(url: String, path: String, headers: Dictionary<String, String>, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let urlWithParams = url + path
        let myURL = NSURL(string: urlWithParams)
        
        action(myURL!, methodName: ntlmMethod.HTTP_GET, headers: headers, data: nil, success: success, failure: failure)
    }
    
    /**
    POST Methods
    :param: url: String                         URL
            path: String                        Parameters for url 
            header: Dictionary<String, String>  Headers request. This can be nil
            data:   Dictionary<String, String>  
            success(NSDictionary)               A callback when the task is successfully completed
            failure(NSError)                    A callback when the task is failed
    :returns: Void
    */
    func NtlmPOST(url: String, path: String, headers: Dictionary<String, String>, data: NSDictionary, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let urlWithParams = url + path
        let myURL = NSURL(string: urlWithParams)
        
        action(myURL!, methodName: ntlmMethod.HTTP_POST, headers: headers, data: data, success: success, failure: failure)
    }
    
    
    /**
    PUT Methods
    :param: url: String                         URL
            path: String                        Parameters for url 
            header: Dictionary<String, String>  Headers request. This can be nil
            data:   Dictionary<String, String>  
            success(NSDictionary)               A callback when the task is successfully completed
            failure(NSError)                    A callback when the task is failed
    :returns: Void
    */
    func NtlmPUT(url: String, path: String, headers: Dictionary<String, String>, data: NSDictionary, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let urlWithParams = url + path
        let myURL = NSURL(string: urlWithParams)
        
        action(myURL!, methodName: ntlmMethod.HTTP_PUT, headers: headers, data: data, success: success, failure: failure)
    }
    
    
    /**
    HEAD Methods
    :param: url: String                         URL
            path: String                        Parameters for url 
            header: Dictionary<String, String>  Headers request. This can be nil
            success(NSDictionary)               A callback when the task is successfully completed
            failure(NSError)                    A callback when the task is failed
    :returns: Void
    */
    func NtlmHEAD(url: String, path: String, headers: Dictionary<String, String>?, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let urlWithParams = url + path
        let myURL = NSURL(string: urlWithParams)
        
        action(myURL!, methodName: ntlmMethod.HTTP_HEAD, headers: headers, data: nil, success: success, failure: failure)
    }
    
    
   /**
    DELETE Methods
    :param: url: String                         URL
            path: String                        Parameters for url 
            header: Dictionary<String, String>  Headers request. This can be nil
            success(NSDictionary)               A callback when the task is successfully completed
            failure(NSError)                    A callback when the task is failed
    :returns: Void
    */
    func NtlmDELETE(url: String, path: String, headers: Dictionary<String, String>?, success: (String)->(String), failure: (NSError)->(NSError)){
        
        let urlWithParams = url + path
        let myURL = NSURL(string: urlWithParams)
        
        action(myURL!, methodName: ntlmMethod.HTTP_DELETE, headers: headers, data: nil, success: success, failure: failure)
    }   
}