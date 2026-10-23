import { useEffect, useState } from "react";
import type { ThemeMode } from "../../core/models";
import { STORAGE_KEYS } from "../../core/constants";

export function useAppTheme(){
    const [theme, setTheme] = useState<ThemeMode>(intialTheme);
    // <html data-theme="light">
    useEffect(()=>{
        document.documentElement.setAttribute("data-theme", theme);

        try{
            localStorage.setItem(STORAGE_KEYS.THEME, theme);
        }catch{}
    },[theme]);

    const toggleTheme = ()=>{
        setTheme((current)=>(current === "dark" ? "light" : "dark"));
    };

    return {
        theme,
        setTheme,
        toggleTheme
    }
}

const intialTheme = ():ThemeMode=>{
    try{
        const stored = localStorage.getItem(STORAGE_KEYS.THEME);

        if(stored === "light" || stored === "dark"){
            return stored;
        }
    }catch{}

    return window.matchMedia("prefers-color-scheme: dark").matches ? "dark" : "light";
}