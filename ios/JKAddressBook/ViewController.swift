//
//  ViewController.swift
//  JKAddressBook
//
//  Created by aven wu on 11/29/15.
//  Copyright © 2015 avenwu. All rights reserved.
//

import UIKit
import Kanna

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        testLogin2()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func getConfiguration() ->NSURLSessionConfiguration {
        let configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        configuration.timeoutIntervalForRequest = 30
        configuration.HTTPCookieAcceptPolicy = NSHTTPCookieAcceptPolicy.Always
        return configuration
    }
    var session:NSURLSession!
    
    func testLogin2() {
        session = NSURLSession(configuration: getConfiguration())
        let url = NSURL(string: "http://work.eoemobile.com/login")
        
        let prepareTask = session.dataTaskWithURL(url!, completionHandler: { [weak self](data:NSData?, response: NSURLResponse?, error:NSError?) -> Void in
            if let csrf_token = self!.parseToken(data!) {
                self!.login(csrf_token)
            } else {
                print("Token can not be nil")
            }
            }
        )
        prepareTask.resume()
    }
    
    func parseToken(html:NSData) -> String? {
        var csrf_param:String?
        var csrf_token:String?
        if let doc = Kanna.HTML(html: html, encoding: NSUTF8StringEncoding) {
            print(doc.title)
            for node in doc.xpath("//meta") {
                print(node["name"])
                print(node["content"])
                if node["name"] == "csrf-param" {
                    csrf_param = node["content"]!
                } else if node["name"] == "csrf-token" {
                    csrf_token = node["content"]!
                }
            }
        }
        print("parse token finished: csrf_param=\(csrf_param), csrf_token=\(csrf_token)")
        return csrf_token
    }
    
    func login(csrf_token : String) {
        var username:String
        var password:String
        username = "123213"
        password = "121121"
        let loginRequest = NSMutableURLRequest(URL: NSURL(string: "http://work.eoemobile.com/login")!)
        loginRequest.HTTPMethod = "POST"
        var params = "utf8=✓&authenticity_token=\(csrf_token)&username=\(username)&password=\(password)&login=Login »"
        print("origin:\(params)")
        params = params.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        print("converted:\(params)")
        let postBody = params.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: false)
        let loginTask = session.uploadTaskWithRequest(loginRequest, fromData: postBody) { (data: NSData?, response: NSURLResponse?,
            error: NSError?) -> Void in
            let str = NSString(data: data!, encoding: NSUTF8StringEncoding)
            print("string=\(str)")
        }
        loginTask.resume()
    }
}

