 1: /* Logger.java -- a class for logging messages
   2:    Copyright (C) 2002, 2004, 2006, 2007 Free Software Foundation, Inc.
   3: 
   4: This file is part of GNU Classpath.
   5: 
   6: GNU Classpath is free software; you can redistribute it and/or modify
   7: it under the terms of the GNU General Public License as published by
   8: the Free Software Foundation; either version 2, or (at your option)
   9: any later version.
  10: 
  11: GNU Classpath is distributed in the hope that it will be useful, but
  12: WITHOUT ANY WARRANTY; without even the implied warranty of
  13: MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  14: General Public License for more details.
  15: 
  16: You should have received a copy of the GNU General Public License
  17: along with GNU Classpath; see the file COPYING.  If not, write to the
  18: Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  19: 02110-1301 USA.
  20: 
  21: Linking this library statically or dynamically with other modules is
  22: making a combined work based on this library.  Thus, the terms and
  23: conditions of the GNU General Public License cover the whole
  24: combination.
  25: 
  26: As a special exception, the copyright holders of this library give you
  27: permission to link this library with independent modules to produce an
  28: executable, regardless of the license terms of these independent
  29: modules, and to copy and distribute the resulting executable under
  30: terms of your choice, provided that you also meet, for each linked
  31: independent module, the terms and conditions of the license of that
  32: module.  An independent module is a module which is not derived from
  33: or based on this library.  If you modify this library, you may extend
  34: this exception to your version of the library, but you are not
  35: obligated to do so.  If you do not wish to do so, delete this
  36: exception statement from your version. */
  37: 
  38: 
  39: package java.util.logging;
  40: 
  41: import java.util.List;
  42: import java.util.MissingResourceException;
  43: import java.util.ResourceBundle;
  44: import java.security.AccessController;
  45: import java.security.PrivilegedAction;
  46: 
  47: /**
  48:  * A Logger is used for logging information about events. Usually, there
  49:  * is a seprate logger for each subsystem or component, although there
  50:  * is a shared instance for components that make only occasional use of
  51:  * the logging framework.
  52:  *
  53:  * <p>It is common to name a logger after the name of a corresponding
  54:  * Java package.  Loggers are organized into a hierarchical namespace;
  55:  * for example, the logger <code>"org.gnu.foo"</code> is the
  56:  * <em>parent</em> of logger <code>"org.gnu.foo.bar"</code>.
  57:  *
  58:  * <p>A logger for a named subsystem can be obtained through {@link
  59:  * java.util.logging.Logger#getLogger(java.lang.String)}.  However,
  60:  * only code which has been granted the permission to control the
  61:  * logging infrastructure will be allowed to customize that logger.
  62:  * Untrusted code can obtain a private, anonymous logger through
  63:  * {@link #getAnonymousLogger()} if it wants to perform any
  64:  * modifications to the logger.
  65:  *
  66:  * <p>FIXME: Write more documentation.
  67:  *
  68:  * @author Sascha Brawer (brawer@acm.org)
  69:  */
  70: public class Logger
  71: {
  72: 
  73:   static final Logger root = new Logger("", null);
  74: 
  75:   /**
  76:    * A logger provided to applications that make only occasional use
  77:    * of the logging framework, typically early prototypes.  Serious
  78:    * products are supposed to create and use their own Loggers, so
  79:    * they can be controlled individually.
  80:    */
  81:   public static final Logger global;
  82: 
  83:   static
  84:     {
  85:       // Our class might be initialized from an unprivileged context
  86:       global = (Logger) AccessController.doPrivileged
  87:     (new PrivilegedAction()
  88:       {
  89:         public Object run()
  90:         {
  91:           return getLogger("global");
  92:         }
  93:       });
  94:     }
  95: 
  96: 
  97:   /**
  98:    * The name of the Logger, or <code>null</code> if the logger is
  99:    * anonymous.
 100:    *
 101:    * <p>A previous version of the GNU Classpath implementation granted
 102:    * untrusted code the permission to control any logger whose name
 103:    * was null.  However, test code revealed that the Sun J2SE 1.4
 104:    * reference implementation enforces the security control for any
 105:    * logger that was not created through getAnonymousLogger, even if
 106:    * it has a null name.  Therefore, a separate flag {@link
 107:    * Logger#anonymous} was introduced.
 108:    */
 109:   private final String name;
 110: 
 111: 
 112:   /**
 113:    * The name of the resource bundle used for localization.
 114:    *
 115:    * <p>This variable cannot be declared as <code>final</code>
 116:    * because its value can change as a result of calling
 117:    * getLogger(String,String).
 118:    */
 119:   private String resourceBundleName;
 120: 
 121: 
 122:   /**
 123:    * The resource bundle used for localization.
 124:    *
 125:    * <p>This variable cannot be declared as <code>final</code>
 126:    * because its value can change as a result of calling
 127:    * getLogger(String,String).
 128:    */
 129:   private ResourceBundle resourceBundle;
 130: 
 131:   private Filter filter;
 132: 
 133:   private final List handlerList = new java.util.ArrayList(4);
 134:   private Handler[] handlers = new Handler[0];
 135: 
 136:   /**
 137:    * Indicates whether or not this logger is anonymous.  While
 138:    * a LoggingPermission is required for any modifications to
 139:    * a normal logger, untrusted code can obtain an anonymous logger
 140:    * and modify it according to its needs.
 141:    *
 142:    * <p>A previous version of the GNU Classpath implementation
 143:    * granted access to every logger whose name was null.
 144:    * However, test code revealed that the Sun J2SE 1.4 reference
 145:    * implementation enforces the security control for any logger
 146:    * that was not created through getAnonymousLogger, even
 147:    * if it has a null name.
 148:    */
 149:   private boolean anonymous;
 150: 
 151: 
 152:   private boolean useParentHandlers;
 153: 
 154:   private Level level;
 155: 
 156:   private Logger parent;
 157: 
 158:   /**
 159:    * Constructs a Logger for a subsystem.  Most applications do not
 160:    * need to create new Loggers explicitly; instead, they should call
 161:    * the static factory methods
 162:    * {@link #getLogger(java.lang.String,java.lang.String) getLogger}
 163:    * (with ResourceBundle for localization) or
 164:    * {@link #getLogger(java.lang.String) getLogger} (without
 165:    * ResourceBundle), respectively.
 166:    *
 167:    * @param name the name for the logger, for example "java.awt"
 168:    *             or "com.foo.bar". The name should be based on
 169:    *             the name of the package issuing log records
 170:    *             and consist of dot-separated Java identifiers.
 171:    *
 172:    * @param resourceBundleName the name of a resource bundle
 173:    *        for localizing messages, or <code>null</code>
 174:    *        to indicate that messages do not need to be localized.
 175:    *
 176:    * @throws java.util.MissingResourceException if
 177:    *         <code>resourceBundleName</code> is not <code>null</code>
 178:    *         and no such bundle could be located.
 179:    */
 180:   protected Logger(String name, String resourceBundleName)
 181:     throws MissingResourceException
 182:   {
 183:     this.name = name;
 184:     this.resourceBundleName = resourceBundleName;
 185: 
 186:     if (resourceBundleName == null)
 187:       resourceBundle = null;
 188:     else
 189:       resourceBundle = ResourceBundle.getBundle(resourceBundleName);
 190: 
 191:     level = null;
 192: 
 193:     /* This is null when the root logger is being constructed,
 194:      * and the root logger afterwards.
 195:      */
 196:     parent = root;
 197: 
 198:     useParentHandlers = (parent != null);
 199:   }
 200: 
 201: 
 202: 
 203:   /**
 204:    * Finds a registered logger for a subsystem, or creates one in
 205:    * case no logger has been registered yet.
 206:    *
 207:    * @param name the name for the logger, for example "java.awt"
 208:    *             or "com.foo.bar". The name should be based on
 209:    *             the name of the package issuing log records
 210:    *             and consist of dot-separated Java identifiers.
 211:    *
 212:    * @throws IllegalArgumentException if a logger for the subsystem
 213:    *         identified by <code>name</code> has already been created,
 214:    *         but uses a a resource bundle for localizing messages.
 215:    *
 216:    * @throws NullPointerException if <code>name</code> is
 217:    *         <code>null</code>.
 218:    *
 219:    * @return a logger for the subsystem specified by <code>name</code>
 220:    *         that does not localize messages.
 221:    */
 222:   public static Logger getLogger(String name)
 223:   {
 224:     return getLogger(name, null);
 225:   }
 226: 
 227:     
 228:   /**
 229:    * Finds a registered logger for a subsystem, or creates one in case
 230:    * no logger has been registered yet.
 231:    *
 232:    * <p>If a logger with the specified name has already been
 233:    * registered, the behavior depends on the resource bundle that is
 234:    * currently associated with the existing logger.
 235:    *
 236:    * <ul><li>If the existing logger uses the same resource bundle as
 237:    * specified by <code>resourceBundleName</code>, the existing logger
 238:    * is returned.</li>
 239:    *
 240:    * <li>If the existing logger currently does not localize messages,
 241:    * the existing logger is modified to use the bundle specified by
 242:    * <code>resourceBundleName</code>.  The existing logger is then
 243:    * returned.  Therefore, all subsystems currently using this logger
 244:    * will produce localized messages from now on.</li>
 245:    *
 246:    * <li>If the existing logger already has an associated resource
 247:    * bundle, but a different one than specified by
 248:    * <code>resourceBundleName</code>, an
 249:    * <code>IllegalArgumentException</code> is thrown.</li></ul>
 250:    *
 251:    * @param name the name for the logger, for example "java.awt"
 252:    *             or "org.gnu.foo". The name should be based on
 253:    *             the name of the package issuing log records
 254:    *             and consist of dot-separated Java identifiers.
 255:    *
 256:    * @param resourceBundleName the name of a resource bundle
 257:    *        for localizing messages, or <code>null</code>
 258:    *        to indicate that messages do not need to be localized.
 259:    *
 260:    * @return a logger for the subsystem specified by <code>name</code>.
 261:    *
 262:    * @throws java.util.MissingResourceException if
 263:    *         <code>resourceBundleName</code> is not <code>null</code>
 264:    *         and no such bundle could be located.   
 265:    *
 266:    * @throws IllegalArgumentException if a logger for the subsystem
 267:    *         identified by <code>name</code> has already been created,
 268:    *         but uses a different resource bundle for localizing
 269:    *         messages.
 270:    *
 271:    * @throws NullPointerException if <code>name</code> is
 272:    *         <code>null</code>.
 273:    */
 274:   public static Logger getLogger(String name, String resourceBundleName)
 275:   {
 276:     LogManager lm = LogManager.getLogManager();
 277:     Logger     result;
 278: 
 279:     if (name == null)
 280:       throw new NullPointerException();
 281: 
 282:     /* Without synchronized(lm), it could happen that another thread
 283:      * would create a logger between our calls to getLogger and
 284:      * addLogger.  While addLogger would indicate this by returning
 285:      * false, we could not be sure that this other logger was still
 286:      * existing when we called getLogger a second time in order
 287:      * to retrieve it -- note that LogManager is only allowed to
 288:      * keep weak references to registered loggers, so Loggers
 289:      * can be garbage collected at any time in general, and between
 290:      * our call to addLogger and our second call go getLogger
 291:      * in particular.
 292:      *
 293:      * Of course, we assume here that LogManager.addLogger etc.
 294:      * are synchronizing on the global LogManager object. There
 295:      * is a comment in the implementation of LogManager.addLogger
 296:      * referring to this comment here, so that any change in
 297:      * the synchronization of LogManager will be reflected here.
 298:      */
 299:     synchronized (lm)
 300:     {
 301:       result = lm.getLogger(name);
 302:       if (result == null)
 303:       {
 304:     boolean couldBeAdded;
 305: 
 306:     result = new Logger(name, resourceBundleName);
 307:     couldBeAdded = lm.addLogger(result);
 308:     if (!couldBeAdded)
 309:       throw new IllegalStateException("cannot register new logger");
 310:       }
 311:       else
 312:       {
 313:     /* The logger already exists. Make sure it uses
 314:      * the same resource bundle for localizing messages.
 315:      */
 316:     String existingBundleName = result.getResourceBundleName();
 317: 
 318:     /* The Sun J2SE 1.4 reference implementation will return the
 319:      * registered logger object, even if it does not have a resource
 320:      * bundle associated with it. However, it seems to change the
 321:      * resourceBundle of the registered logger to the bundle
 322:      * whose name was passed to getLogger.
 323:      */
 324:     if ((existingBundleName == null) && (resourceBundleName != null))
 325:     {
 326:       /* If ResourceBundle.getBundle throws an exception, the
 327:        * existing logger will be unchanged.  This would be
 328:        * different if the assignment to resourceBundleName
 329:        * came first.
 330:        */
 331:       result.resourceBundle = ResourceBundle.getBundle(resourceBundleName);
 332:       result.resourceBundleName = resourceBundleName;
 333:       return result;
 334:     }
 335: 
 336:     if ((existingBundleName != resourceBundleName)
 337:         && ((existingBundleName == null)
 338:         || !existingBundleName.equals(resourceBundleName)))
 339:     {
 340:       throw new IllegalArgumentException();
 341:     }
 342:       }
 343:     }
 344: 
 345:     return result;
 346:   }
 347: 
 348:   
 349:   /**
 350:    * Creates a new, unnamed logger.  Unnamed loggers are not
 351:    * registered in the namespace of the LogManager, and no special
 352:    * security permission is required for changing their state.
 353:    * Therefore, untrusted applets are able to modify their private
 354:    * logger instance obtained through this method.
 355:    *
 356:    * <p>The parent of the newly created logger will the the root
 357:    * logger, from which the level threshold and the handlers are
 358:    * inherited.
 359:    */
 360:   public static Logger getAnonymousLogger()
 361:   {
 362:     return getAnonymousLogger(null);
 363:   }
 364: 
 365: 
 366:   /**
 367:    * Creates a new, unnamed logger.  Unnamed loggers are not
 368:    * registered in the namespace of the LogManager, and no special
 369:    * security permission is required for changing their state.
 370:    * Therefore, untrusted applets are able to modify their private
 371:    * logger instance obtained through this method.
 372:    *
 373:    * <p>The parent of the newly created logger will the the root
 374:    * logger, from which the level threshold and the handlers are
 375:    * inherited.
 376:    *
 377:    * @param resourceBundleName the name of a resource bundle
 378:    *        for localizing messages, or <code>null</code>
 379:    *        to indicate that messages do not need to be localized.
 380:    *
 381:    * @throws java.util.MissingResourceException if
 382:    *         <code>resourceBundleName</code> is not <code>null</code>
 383:    *         and no such bundle could be located.
 384:    */
 385:   public static Logger getAnonymousLogger(String resourceBundleName)
 386:     throws MissingResourceException
 387:   {
 388:     Logger  result;
 389: 
 390:     result = new Logger(null, resourceBundleName);
 391:     result.anonymous = true;
 392:     return result;
 393:   }
 394: 
 395: 
 396:   /**
 397:    * Returns the name of the resource bundle that is being used for
 398:    * localizing messages.
 399:    *
 400:    * @return the name of the resource bundle used for localizing messages,
 401:    *         or <code>null</code> if the parent's resource bundle
 402:    *         is used for this purpose.
 403:    */
 404:   public synchronized String getResourceBundleName()
 405:   {
 406:     return resourceBundleName;
 407:   }
 408: 
 409: 
 410:   /**
 411:    * Returns the resource bundle that is being used for localizing
 412:    * messages.
 413:    *
 414:    * @return the resource bundle used for localizing messages,
 415:    *         or <code>null</code> if the parent's resource bundle
 416:    *         is used for this purpose.
 417:    */
 418:   public synchronized ResourceBundle getResourceBundle()
 419:   {
 420:     return resourceBundle;
 421:   }
 422: 
 423: 
 424:   /**
 425:    * Returns the severity level threshold for this <code>Handler</code>.
 426:    * All log records with a lower severity level will be discarded;
 427:    * a log record of the same or a higher level will be published
 428:    * unless an installed <code>Filter</code> decides to discard it.
 429:    *
 430:    * @return the severity level below which all log messages will be
 431:    *         discarded, or <code>null</code> if the logger inherits
 432:    *         the threshold from its parent.
 433:    */
 434:   public synchronized Level getLevel()
 435:   {
 436:     return level;
 437:   }
 438: 
 439: 
 440:   /**
 441:    * Returns whether or not a message of the specified level
 442:    * would be logged by this logger.
 443:    *
 444:    * @throws NullPointerException if <code>level</code>
 445:    *         is <code>null</code>.
 446:    */
 447:   public synchronized boolean isLoggable(Level level)
 448:   {
 449:     if (this.level != null)
 450:       return this.level.intValue() <= level.intValue();
 451: 
 452:     if (parent != null)
 453:       return parent.isLoggable(level);
 454:     else
 455:       return false;
 456:   }
 457: 
 458: 
 459:   /**
 460:    * Sets the severity level threshold for this <code>Handler</code>.
 461:    * All log records with a lower severity level will be discarded
 462:    * immediately.  A log record of the same or a higher level will be
 463:    * published unless an installed <code>Filter</code> decides to
 464:    * discard it.
 465:    *
 466:    * @param level the severity level below which all log messages
 467:    *              will be discarded, or <code>null</code> to
 468:    *              indicate that the logger should inherit the
 469:    *              threshold from its parent.
 470:    *
 471:    * @throws SecurityException if this logger is not anonymous, a
 472:    *     security manager exists, and the caller is not granted
 473:    *     the permission to control the logging infrastructure by
 474:    *     having LoggingPermission("control").  Untrusted code can
 475:    *     obtain an anonymous logger through the static factory method
 476:    *     {@link #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
 477:    */
 478:   public synchronized void setLevel(Level level)
 479:   {
 480:     /* An application is allowed to control an anonymous logger
 481:      * without having the permission to control the logging
 482:      * infrastructure.
 483:      */
 484:     if (!anonymous)
 485:       LogManager.getLogManager().checkAccess();
 486: 
 487:     this.level = level;
 488:   }
 489: 
 490: 
 491:   public synchronized Filter getFilter()
 492:   {
 493:     return filter;
 494:   }
 495: 
 496: 
 497:   /**
 498:    * @throws SecurityException if this logger is not anonymous, a
 499:    *     security manager exists, and the caller is not granted
 500:    *     the permission to control the logging infrastructure by
 501:    *     having LoggingPermission("control").  Untrusted code can
 502:    *     obtain an anonymous logger through the static factory method
 503:    *     {@link #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
 504:    */
 505:   public synchronized void setFilter(Filter filter)
 506:     throws SecurityException
 507:   {
 508:     /* An application is allowed to control an anonymous logger
 509:      * without having the permission to control the logging
 510:      * infrastructure.
 511:      */
 512:     if (!anonymous)
 513:       LogManager.getLogManager().checkAccess();
 514: 
 515:     this.filter = filter;
 516:   }
 517: 
 518: 
 519: 
 520: 
 521:   /**
 522:    * Returns the name of this logger.
 523:    *
 524:    * @return the name of this logger, or <code>null</code> if
 525:    *         the logger is anonymous.
 526:    */
 527:   public String getName()
 528:   {
 529:     /* Note that the name of a logger cannot be changed during
 530:      * its lifetime, so no synchronization is needed.
 531:      */
 532:     return name;
 533:   }
 534: 
 535: 
 536:   /**
 537:    * Passes a record to registered handlers, provided the record
 538:    * is considered as loggable both by {@link #isLoggable(Level)}
 539:    * and a possibly installed custom {@link #setFilter(Filter) filter}.
 540:    *
 541:    * <p>If the logger has been configured to use parent handlers,
 542:    * the record will be forwarded to the parent of this logger
 543:    * in addition to being processed by the handlers registered with
 544:    * this logger.
 545:    *
 546:    * <p>The other logging methods in this class are convenience methods
 547:    * that merely create a new LogRecord and pass it to this method.
 548:    * Therefore, subclasses usually just need to override this single
 549:    * method for customizing the logging behavior.
 550:    *
 551:    * @param record the log record to be inspected and possibly forwarded.
 552:    */
 553:   public synchronized void log(LogRecord record)
 554:   {
 555:     if (!isLoggable(record.getLevel()))
 556:       return;
 557: 
 558:     if ((filter != null) && !filter.isLoggable(record))
 559:       return;
 560: 
 561:     /* If no logger name has been set for the log record,
 562:      * use the name of this logger.
 563:      */
 564:     if (record.getLoggerName() == null)
 565:       record.setLoggerName(name);
 566: 
 567:     /* Avoid that some other thread is changing the logger hierarchy
 568:      * while we are traversing it.
 569:      */
 570:     synchronized (LogManager.getLogManager())
 571:     {
 572:       Logger curLogger = this;
 573: 
 574:       do
 575:       {
 576:         /* The Sun J2SE 1.4 reference implementation seems to call the
 577:      * filter only for the logger whose log method is called,
 578:      * never for any of its parents.  Also, parent loggers publish
 579:      * log record whatever their level might be.  This is pretty
 580:      * weird, but GNU Classpath tries to be as compatible as
 581:      * possible to the reference implementation.
 582:      */
 583:         for (int i = 0; i < curLogger.handlers.length; i++)
 584:           curLogger.handlers[i].publish(record);
 585: 
 586:     if (curLogger.getUseParentHandlers() == false)
 587:       break;
 588:     
 589:     curLogger = curLogger.getParent();
 590:       }
 591:       while (parent != null);
 592:     }
 593:   }
 594: 
 595: 
 596:   public void log(Level level, String message)
 597:   {
 598:     if (isLoggable(level))
 599:       log(level, message, (Object[]) null);
 600:   }
 601: 
 602: 
 603:   public synchronized void log(Level level,
 604:                    String message,
 605:                    Object param)
 606:   {
 607:     if (isLoggable(level))
 608:       {
 609:         StackTraceElement caller = getCallerStackFrame();
 610:         logp(level,
 611:              caller != null ? caller.getClassName() : "<unknown>",
 612:              caller != null ? caller.getMethodName() : "<unknown>",
 613:              message,
 614:              param);
 615:       }
 616:   }
 617: 
 618: 
 619:   public synchronized void log(Level level,
 620:                    String message,
 621:                    Object[] params)
 622:   {
 623:     if (isLoggable(level))
 624:       {
 625:         StackTraceElement caller = getCallerStackFrame();
 626:         logp(level,
 627:              caller != null ? caller.getClassName() : "<unknown>",
 628:              caller != null ? caller.getMethodName() : "<unknown>",
 629:              message,
 630:              params);
 631:       }
 632:   }
 633: 
 634: 
 635:   public synchronized void log(Level level,
 636:                    String message,
 637:                    Throwable thrown)
 638:   {
 639:     if (isLoggable(level))
 640:       {
 641:         StackTraceElement caller = getCallerStackFrame();    
 642:         logp(level,
 643:              caller != null ? caller.getClassName() : "<unknown>",
 644:              caller != null ? caller.getMethodName() : "<unknown>",
 645:              message,
 646:              thrown);
 647:       }
 648:   }
 649: 
 650: 
 651:   public synchronized void logp(Level level,
 652:                 String sourceClass,
 653:                 String sourceMethod,
 654:                 String message)
 655:   {
 656:     logp(level, sourceClass, sourceMethod, message,
 657:      (Object[]) null);
 658:   }
 659: 
 660: 
 661:   public synchronized void logp(Level level,
 662:                 String sourceClass,
 663:                 String sourceMethod,
 664:                 String message,
 665:                 Object param)
 666:   {
 667:     logp(level, sourceClass, sourceMethod, message,
 668:      new Object[] { param });
 669:   }
 670: 
 671: 
 672:   private synchronized ResourceBundle findResourceBundle()
 673:   {
 674:     if (resourceBundle != null)
 675:       return resourceBundle;
 676: 
 677:     if (parent != null)
 678:       return parent.findResourceBundle();
 679: 
 680:     return null;
 681:   }
 682: 
 683: 
 684:   private synchronized void logImpl(Level level,
 685:                     String sourceClass,
 686:                     String sourceMethod,
 687:                     String message,
 688:                     Object[] params)
 689:   {
 690:     LogRecord rec = new LogRecord(level, message);
 691: 
 692:     rec.setResourceBundle(findResourceBundle());
 693:     rec.setSourceClassName(sourceClass);
 694:     rec.setSourceMethodName(sourceMethod);
 695:     rec.setParameters(params);
 696: 
 697:     log(rec);
 698:   }
 699: 
 700: 
 701:   public synchronized void logp(Level level,
 702:                 String sourceClass,
 703:                 String sourceMethod,
 704:                 String message,
 705:                 Object[] params)
 706:   {
 707:     logImpl(level, sourceClass, sourceMethod, message, params);
 708:   }
 709: 
 710: 
 711:   public synchronized void logp(Level level,
 712:                 String sourceClass,
 713:                 String sourceMethod,
 714:                 String message,
 715:                 Throwable thrown)
 716:   {
 717:     LogRecord rec = new LogRecord(level, message);
 718: 
 719:     rec.setResourceBundle(resourceBundle);
 720:     rec.setSourceClassName(sourceClass);
 721:     rec.setSourceMethodName(sourceMethod);
 722:     rec.setThrown(thrown);
 723: 
 724:     log(rec);
 725:   }
 726: 
 727: 
 728:   public synchronized void logrb(Level level,
 729:                  String sourceClass,
 730:                  String sourceMethod,
 731:                  String bundleName,
 732:                  String message)
 733:   {
 734:     logrb(level, sourceClass, sourceMethod, bundleName,
 735:       message, (Object[]) null);
 736:   }
 737: 
 738: 
 739:   public synchronized void logrb(Level level,
 740:                  String sourceClass,
 741:                  String sourceMethod,
 742:                  String bundleName,
 743:                  String message,
 744:                  Object param)
 745:   {
 746:     logrb(level, sourceClass, sourceMethod, bundleName,
 747:       message, new Object[] { param });
 748:   }
 749: 
 750: 
 751:   public synchronized void logrb(Level level,
 752:                  String sourceClass,
 753:                  String sourceMethod,
 754:                  String bundleName,
 755:                  String message,
 756:                  Object[] params)
 757:   {
 758:     LogRecord rec = new LogRecord(level, message);
 759: 
 760:     rec.setResourceBundleName(bundleName);
 761:     rec.setSourceClassName(sourceClass);
 762:     rec.setSourceMethodName(sourceMethod);
 763:     rec.setParameters(params);
 764: 
 765:     log(rec);
 766:   }
 767: 
 768: 
 769:   public synchronized void logrb(Level level,
 770:                  String sourceClass,
 771:                  String sourceMethod,
 772:                  String bundleName,
 773:                  String message,
 774:                  Throwable thrown)
 775:   {
 776:     LogRecord rec = new LogRecord(level, message);
 777: 
 778:     rec.setResourceBundleName(bundleName);
 779:     rec.setSourceClassName(sourceClass);
 780:     rec.setSourceMethodName(sourceMethod);
 781:     rec.setThrown(thrown);
 782: 
 783:     log(rec);
 784:   }
 785: 
 786: 
 787:   public synchronized void entering(String sourceClass,
 788:                     String sourceMethod)
 789:   {
 790:     if (isLoggable(Level.FINER))
 791:       logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
 792:   }
 793: 
 794: 
 795:   public synchronized void entering(String sourceClass,
 796:                     String sourceMethod,
 797:                     Object param)
 798:   {
 799:     if (isLoggable(Level.FINER))
 800:       logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param);
 801:   }
 802: 
 803: 
 804:   public synchronized void entering(String sourceClass,
 805:                     String sourceMethod,
 806:                     Object[] params)
 807:   {
 808:     if (isLoggable(Level.FINER))
 809:     {
 810:       StringBuffer buf = new StringBuffer(80);
 811:       buf.append("ENTRY");
 812:       for (int i = 0; i < params.length; i++)
 813:       {
 814:     buf.append(" {");
 815:     buf.append(i);
 816:     buf.append('}');
 817:       }
 818:       
 819:       logp(Level.FINER, sourceClass, sourceMethod, buf.toString(), params);
 820:     }
 821:   }
 822: 
 823: 
 824:   public synchronized void exiting(String sourceClass,
 825:                    String sourceMethod)
 826:   {
 827:     if (isLoggable(Level.FINER))
 828:       logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
 829:   }
 830: 
 831:    
 832:   public synchronized void exiting(String sourceClass,
 833:                    String sourceMethod,
 834:                    Object result)
 835:   {
 836:     if (isLoggable(Level.FINER))
 837:       logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
 838:   }
 839: 
 840:  
 841:   public synchronized void throwing(String sourceClass,
 842:                     String sourceMethod,
 843:                     Throwable thrown)
 844:   {
 845:     if (isLoggable(Level.FINER))
 846:       logp(Level.FINER, sourceClass, sourceMethod, "THROW", thrown);
 847:   }
 848: 
 849: 
 850:   /**
 851:    * Logs a message with severity level SEVERE, indicating a serious
 852:    * failure that prevents normal program execution.  Messages at this
 853:    * level should be understandable to an inexperienced, non-technical
 854:    * end user.  Ideally, they explain in simple words what actions the
 855:    * user can take in order to resolve the problem.
 856:    *
 857:    * @see Level#SEVERE
 858:    *
 859:    * @param message the message text, also used as look-up key if the
 860:    *                logger is localizing messages with a resource
 861:    *                bundle.  While it is possible to pass
 862:    *                <code>null</code>, this is not recommended, since
 863:    *                a logging message without text is unlikely to be
 864:    *                helpful.
 865:    */
 866:   public synchronized void severe(String message)
 867:   {
 868:     if (isLoggable(Level.SEVERE))
 869:       log(Level.SEVERE, message);
 870:   }
 871: 
 872: 
 873:   /**
 874:    * Logs a message with severity level WARNING, indicating a
 875:    * potential problem that does not prevent normal program execution.
 876:    * Messages at this level should be understandable to an
 877:    * inexperienced, non-technical end user.  Ideally, they explain in
 878:    * simple words what actions the user can take in order to resolve
 879:    * the problem.
 880:    *
 881:    * @see Level#WARNING
 882:    *
 883:    * @param message the message text, also used as look-up key if the
 884:    *                logger is localizing messages with a resource
 885:    *                bundle.  While it is possible to pass
 886:    *                <code>null</code>, this is not recommended, since
 887:    *                a logging message without text is unlikely to be
 888:    *                helpful.
 889:    */
 890:   public synchronized void warning(String message)
 891:   {
 892:     if (isLoggable(Level.WARNING))
 893:       log(Level.WARNING, message);
 894:   }
 895: 
 896: 
 897:   /**
 898:    * Logs a message with severity level INFO.  {@link Level#INFO} is
 899:    * intended for purely informational messages that do not indicate
 900:    * error or warning situations. In the default logging
 901:    * configuration, INFO messages will be written to the system
 902:    * console.  For this reason, the INFO level should be used only for
 903:    * messages that are important to end users and system
 904:    * administrators.  Messages at this level should be understandable
 905:    * to an inexperienced, non-technical user.
 906:    *
 907:    * @param message the message text, also used as look-up key if the
 908:    *                logger is localizing messages with a resource
 909:    *                bundle.  While it is possible to pass
 910:    *                <code>null</code>, this is not recommended, since
 911:    *                a logging message without text is unlikely to be
 912:    *                helpful.
 913:    */
 914:   public synchronized void info(String message)
 915:   {
 916:     if (isLoggable(Level.INFO))
 917:       log(Level.INFO, message);
 918:   }
 919: 
 920: 
 921:   /**
 922:    * Logs a message with severity level CONFIG.  {@link Level#CONFIG} is
 923:    * intended for static configuration messages, for example about the
 924:    * windowing environment, the operating system version, etc.
 925:    *
 926:    * @param message the message text, also used as look-up key if the
 927:    *     logger is localizing messages with a resource bundle.  While
 928:    *     it is possible to pass <code>null</code>, this is not
 929:    *     recommended, since a logging message without text is unlikely
 930:    *     to be helpful.
 931:    */
 932:   public synchronized void config(String message)
 933:   {
 934:     if (isLoggable(Level.CONFIG))
 935:       log(Level.CONFIG, message);
 936:   }
 937: 
 938: 
 939:   /**
 940:    * Logs a message with severity level FINE.  {@link Level#FINE} is
 941:    * intended for messages that are relevant for developers using
 942:    * the component generating log messages. Examples include minor,
 943:    * recoverable failures, or possible inefficiencies.
 944:    *
 945:    * @param message the message text, also used as look-up key if the
 946:    *                logger is localizing messages with a resource
 947:    *                bundle.  While it is possible to pass
 948:    *                <code>null</code>, this is not recommended, since
 949:    *                a logging message without text is unlikely to be
 950:    *                helpful.
 951:    */
 952:   public synchronized void fine(String message)
 953:   {
 954:     if (isLoggable(Level.FINE))
 955:       log(Level.FINE, message);
 956:   }
 957: 
 958: 
 959:   /**
 960:    * Logs a message with severity level FINER.  {@link Level#FINER} is
 961:    * intended for rather detailed tracing, for example entering a
 962:    * method, returning from a method, or throwing an exception.
 963:    *
 964:    * @param message the message text, also used as look-up key if the
 965:    *                logger is localizing messages with a resource
 966:    *                bundle.  While it is possible to pass
 967:    *                <code>null</code>, this is not recommended, since
 968:    *                a logging message without text is unlikely to be
 969:    *                helpful.
 970:    */
 971:   public synchronized void finer(String message)
 972:   {
 973:     if (isLoggable(Level.FINER))
 974:       log(Level.FINER, message);
 975:   }
 976: 
 977: 
 978:   /**
 979:    * Logs a message with severity level FINEST.  {@link Level#FINEST}
 980:    * is intended for highly detailed tracing, for example reaching a
 981:    * certain point inside the body of a method.
 982:    *
 983:    * @param message the message text, also used as look-up key if the
 984:    *                logger is localizing messages with a resource
 985:    *                bundle.  While it is possible to pass
 986:    *                <code>null</code>, this is not recommended, since
 987:    *                a logging message without text is unlikely to be
 988:    *                helpful.
 989:    */
 990:   public synchronized void finest(String message)
 991:   {
 992:     if (isLoggable(Level.FINEST))
 993:       log(Level.FINEST, message);
 994:   }
 995: 
 996: 
 997:   /**
 998:    * Adds a handler to the set of handlers that get notified
 999:    * when a log record is to be published.
1000:    *
1001:    * @param handler the handler to be added.
1002:    *
1003:    * @throws NullPointerException if <code>handler</code>
1004:    *     is <code>null</code>.
1005:    *
1006:    * @throws SecurityException if this logger is not anonymous, a
1007:    *     security manager exists, and the caller is not granted
1008:    *     the permission to control the logging infrastructure by
1009:    *     having LoggingPermission("control").  Untrusted code can
1010:    *     obtain an anonymous logger through the static factory method
1011:    *     {@link #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
1012:    */
1013:   public synchronized void addHandler(Handler handler)
1014:     throws SecurityException
1015:   {
1016:     if (handler == null)
1017:       throw new NullPointerException();
1018: 
1019:     /* An application is allowed to control an anonymous logger
1020:      * without having the permission to control the logging
1021:      * infrastructure.
1022:      */
1023:     if (!anonymous)
1024:       LogManager.getLogManager().checkAccess();
1025: 
1026:     if (!handlerList.contains(handler))
1027:     {
1028:       handlerList.add(handler);
1029:       handlers = getHandlers();
1030:     }
1031:   }
1032: 
1033: 
1034:   /**
1035:    * Removes a handler from the set of handlers that get notified
1036:    * when a log record is to be published.
1037:    *
1038:    * @param handler the handler to be removed.
1039:    *
1040:    * @throws SecurityException if this logger is not anonymous, a
1041:    *     security manager exists, and the caller is not granted the
1042:    *     permission to control the logging infrastructure by having
1043:    *     LoggingPermission("control").  Untrusted code can obtain an
1044:    *     anonymous logger through the static factory method {@link
1045:    *     #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
1046:    *
1047:    * @throws NullPointerException if <code>handler</code>
1048:    *     is <code>null</code>.
1049:    */
1050:   public synchronized void removeHandler(Handler handler)
1051:     throws SecurityException
1052:   {
1053:     /* An application is allowed to control an anonymous logger
1054:      * without having the permission to control the logging
1055:      * infrastructure.
1056:      */
1057:     if (!anonymous)
1058:       LogManager.getLogManager().checkAccess();
1059: 
1060:     if (handler == null)
1061:       throw new NullPointerException();
1062: 
1063:     handlerList.remove(handler);
1064:     handlers = getHandlers();
1065:   }
1066: 
1067: 
1068:   /**
1069:    * Returns the handlers currently registered for this Logger.
1070:    * When a log record has been deemed as being loggable,
1071:    * it will be passed to all registered handlers for
1072:    * publication.  In addition, if the logger uses parent handlers
1073:    * (see {@link #getUseParentHandlers() getUseParentHandlers}
1074:    * and {@link #setUseParentHandlers(boolean) setUseParentHandlers},
1075:    * the log record will be passed to the parent's handlers.
1076:    */
1077:   public synchronized Handler[] getHandlers()
1078:   {
1079:     /* We cannot return our internal handlers array
1080:      * because we do not have any guarantee that the
1081:      * caller would not change the array entries.
1082:      */
1083:     return (Handler[]) handlerList.toArray(new Handler[handlerList.size()]);
1084:   }
1085: 
1086: 
1087:   /**
1088:    * Returns whether or not this Logger forwards log records to
1089:    * handlers registered for its parent loggers.
1090:    *
1091:    * @return <code>false</code> if this Logger sends log records
1092:    *         merely to Handlers registered with itself;
1093:    *         <code>true</code> if this Logger sends log records
1094:    *         not only to Handlers registered with itself, but also
1095:    *         to those Handlers registered with parent loggers.
1096:    */
1097:   public synchronized boolean getUseParentHandlers()
1098:   {
1099:     return useParentHandlers;
1100:   }
1101: 
1102: 
1103:   /**
1104:    * Sets whether or not this Logger forwards log records to
1105:    * handlers registered for its parent loggers.
1106:    *
1107:    * @param useParentHandlers <code>false</code> to let this
1108:    *         Logger send log records merely to Handlers registered
1109:    *         with itself; <code>true</code> to let this Logger
1110:    *         send log records not only to Handlers registered
1111:    *         with itself, but also to those Handlers registered with
1112:    *         parent loggers.
1113:    *
1114:    * @throws SecurityException if this logger is not anonymous, a
1115:    *     security manager exists, and the caller is not granted
1116:    *     the permission to control the logging infrastructure by
1117:    *     having LoggingPermission("control").  Untrusted code can
1118:    *     obtain an anonymous logger through the static factory method
1119:    *     {@link #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
1120:    *
1121:    */
1122:   public synchronized void setUseParentHandlers(boolean useParentHandlers)
1123:   {
1124:     /* An application is allowed to control an anonymous logger
1125:      * without having the permission to control the logging
1126:      * infrastructure.
1127:      */
1128:     if (!anonymous)
1129:       LogManager.getLogManager().checkAccess();
1130: 
1131:     this.useParentHandlers = useParentHandlers;
1132:   }
1133: 
1134: 
1135:   /**
1136:    * Returns the parent of this logger.  By default, the parent is
1137:    * assigned by the LogManager by inspecting the logger's name.
1138:    *
1139:    * @return the parent of this logger (as detemined by the LogManager
1140:    *     by inspecting logger names), the root logger if no other
1141:    *     logger has a name which is a prefix of this logger's name, or
1142:    *     <code>null</code> for the root logger.
1143:    */
1144:   public synchronized Logger getParent()
1145:   {
1146:     return parent;
1147:   }
1148: 
1149: 
1150:   /**
1151:    * Sets the parent of this logger.  Usually, applications do not
1152:    * call this method directly.  Instead, the LogManager will ensure
1153:    * that the tree of loggers reflects the hierarchical logger
1154:    * namespace.  Basically, this method should not be public at all,
1155:    * but the GNU implementation follows the API specification.
1156:    *
1157:    * @throws NullPointerException if <code>parent</code> is
1158:    *     <code>null</code>.
1159:    *
1160:    * @throws SecurityException if this logger is not anonymous, a
1161:    *     security manager exists, and the caller is not granted
1162:    *     the permission to control the logging infrastructure by
1163:    *     having LoggingPermission("control").  Untrusted code can
1164:    *     obtain an anonymous logger through the static factory method
1165:    *     {@link #getAnonymousLogger(java.lang.String) getAnonymousLogger}.
1166:    */
1167:   public synchronized void setParent(Logger parent)
1168:   {
1169:     if (parent == null)
1170:       throw new NullPointerException();
1171: 
1172:     if (this == root)
1173:         throw new IllegalArgumentException(
1174:           "the root logger can only have a null parent");
1175: 
1176:     /* An application is allowed to control an anonymous logger
1177:      * without having the permission to control the logging
1178:      * infrastructure.
1179:      */
1180:     if (!anonymous)
1181:       LogManager.getLogManager().checkAccess();
1182: 
1183:     this.parent = parent;
1184:   }
1185:   
1186:   /**
1187:    * Gets the StackTraceElement of the first class that is not this class.
1188:    * That should be the initial caller of a logging method.
1189:    * @return caller of the initial logging method or null if unknown.
1190:    */
1191:   private StackTraceElement getCallerStackFrame()
1192:   {
1193:     Throwable t = new Throwable();
1194:     StackTraceElement[] stackTrace = t.getStackTrace();
1195:     int index = 0;
1196: 
1197:     // skip to stackentries until this class
1198:     while(index < stackTrace.length
1199:       && !stackTrace[index].getClassName().equals(getClass().getName()))
1200:       index++;
1201: 
1202:     // skip the stackentries of this class
1203:     while(index < stackTrace.length
1204:       && stackTrace[index].getClassName().equals(getClass().getName()))
1205:       index++;
1206: 
1207:     return index < stackTrace.length ? stackTrace[index] : null;
1208:   }
1209:   
1210:   /**
1211:    * Reset and close handlers attached to this logger. This function is package
1212:    * private because it must only be available to the LogManager.
1213:    */
1214:   void resetLogger()
1215:   {
1216:     for (int i = 0; i < handlers.length; i++)
1217:       {
1218:         handlers[i].close();
1219:         handlerList.remove(handlers[i]);
1220:       }
1221:     handlers = getHandlers();
1222:   }
1223: }