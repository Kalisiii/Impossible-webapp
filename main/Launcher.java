package main;

import com.google.gson.Gson;
import model.Data;
import model.Data2;
import model.Kategorija;
import model.Proizvod;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.*;

public class Launcher {
    public static void main(String[] args) {
        staticFiles.location("/public"); //staticka lokacija
        String path = "proizvodi.json"; //putanja za JSON
        port(5000);

        HashMap<String, Object> polja = new HashMap<>();
        //podesena vidljivost proizvoda na pocetnoj strani
        get("/",(request, response) -> {
            ArrayList<Proizvod> lista = new ArrayList<>();
            for (Proizvod p: Data.readFromJson(path)) {
                if (p.getVidljivost()){
                    lista.add(p);
                }
            }
            polja.put("proizvod", lista);
            return new ModelAndView(polja,"index.hbs");
        },new HandlebarsTemplateEngine());




        //pretraga prouzvoda i vraćanje pogleda
        get ("/pretraga",(request, response) -> {
            String pretrazivanje=request.queryParams("pretraga");
            ArrayList<Proizvod> pr =Data.readFromJson(path);
            polja.put("proizvod",pr.stream().filter(p->p.getIme().toUpperCase().contains(pretrazivanje.toUpperCase()) || p.getOpis().toUpperCase().contains(pretrazivanje.toUpperCase())).toArray());
            return new ModelAndView(polja,"index.hbs");
        },new HandlebarsTemplateEngine());

        get("/login", (request, response) -> {
            return new ModelAndView(null,"login.hbs");
        }, new HandlebarsTemplateEngine());


        post("/login2", (request, response) -> {
            String korisnickoIme = request.queryParams("user");
            String lozinka = request.queryParams("lozinka");
            if (korisnickoIme.equals("jana") && lozinka.equals("password")){
                request.session().attribute("user","jana");
                response.redirect("/adminPanel");
            }
            return new ModelAndView(null,"login.hbs");
        }, new HandlebarsTemplateEngine());

//        get("/adminPanel", (request, response) -> {
//            polja.put("proizvodi",Data.readFromJson(path));
//            polja.put("kategorija",Data.readFromJson("kategorije.json"));
//            return new ModelAndView(polja,"adminPanel.hbs");
//
//        }, new HandlebarsTemplateEngine());

        //zasticena sesija na adminPanelu, prikaz kategrije i dodeljivanje proizvodu
        get("/adminPanel", (request, response) -> {
            if(request.session().attribute("user")==null){
                response.redirect("/");
                return null;
            }
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            ArrayList<String> nazivKat = new ArrayList<>();
            for (Kategorija k : kategorije) {
                if(!nazivKat.contains(k.getNaziv())) {
                    nazivKat.add(k.getNaziv());
                }

            }
            polja.put("kategorija",nazivKat);
            polja.put("proizvodi",Data.readFromJson(path));
            return new ModelAndView(polja,"adminPanel.hbs");
        },new HandlebarsTemplateEngine());




        //uzmi proizvod, kategorija se dodeljuje proizvodu
        get("/uzmiProizvod",(request, response) -> {
            response.type("text/text");
            String kategorija = request.queryParams("kategorija").toUpperCase();
            ArrayList<Proizvod> lista = new ArrayList<>();
//            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
            for (Proizvod p : Data.readFromJson(path)) {
                String ime = p.getIme().toUpperCase();
                if(ime.contains(kategorija)) {
                    lista.add(p);
                }
            }
            if(kategorija.equals("0")) {
                lista = Data.readFromJson(path);
            }
            System.out.println(lista.toString());
            Gson gson=new Gson();
            return gson.toJson(lista);
        });

        //odjava i pogled, zasticena sesija
        get("/odjava",(request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/");
            return null;
        },new HandlebarsTemplateEngine());


        //sortiranje proizvoda po ceni i imenu
        get("/sortiraj",(request,response)->{
            response.type("text/text");
            String sortiranje=request.queryParams("sort");
            ArrayList<Proizvod> lista=Data.readFromJson(path);
            for(int i=0;i<lista.size()-1;i++) {
                for(int j=i+1;j<lista.size();j++) {
                    if(sortiranje.equals("3")) {
                        if(lista.get(i).getCena() > lista.get(j).getCena()) {
                            Proizvod tmp=lista.get(i);
                            lista.set(i, lista.get(j));
                            lista.set(j,tmp);
                        }
                    }
                    if(sortiranje.equals("4")) {
                        if(lista.get(i).getCena() < lista.get(j).getCena()) {
                            Proizvod tmp=lista.get(i);
                            lista.set(i, lista.get(j));
                            lista.set(j,tmp);
                        }
                    }
                    if(sortiranje.equals("1")) {
                        if(lista.get(i).getIme().compareTo(lista.get(j).getIme()) > 0) {
                            Proizvod tmp=lista.get(i);
                            lista.set(i, lista.get(j));
                            lista.set(j,tmp);
                        }
                    }
                    if(sortiranje.equals("2")) {
                        if(lista.get(i).getIme().compareTo(lista.get(j).getIme()) < 0) {
                            Proizvod tmp=lista.get(i);
                            lista.set(i, lista.get(j));
                            lista.set(j,tmp);
                        }
                    }

                }
            }
            Gson gson=new Gson();
            return gson.toJson(lista);
        });


        // dodati proizvodi, citanje i upisivanje u JSON preko putanje
        post("/dodato", (request, response) -> {
            String nazivProizvoda = request.queryParams("ime");
            Double cenaProizvoda = Double.parseDouble(request.queryParams("cena"));
            String opisProizvoda = request.queryParams("opis");
            String slikaProizvoda = request.queryParams("slika");
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            Proizvod p = new Proizvod(lista.size() + 1, nazivProizvoda, slikaProizvoda, cenaProizvoda, opisProizvoda);
            if (!lista.contains(p)) {
                lista.add(p);
            }

            Data.writeToJSON(lista ,path);
            return new ModelAndView(null,"dodajProizvod.hbs");
        }, new HandlebarsTemplateEngine());


        //sablon za dodaj proizvod i zasticena sesija
        get("dodajProizvod",(request, response) -> {
            if(request.session().attribute("user")==null){
                response.redirect("/");
                return null;

            }
            return new ModelAndView(null, "dodajProizvod.hbs");
        }, new HandlebarsTemplateEngine());


        //šablon za izmenu proizvoda i zasticena sesija
        get("/izmena/:id", (request, response) -> {
            if(request.session().attribute("user")==null){
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params("id"));
            for (Proizvod p:Data.readFromJson(path)) {
                if (p.getId() == id) {
                    polja.put("proizvod",p);
                }

            }
            return new ModelAndView(polja,"izmena.hbs");
        }, new HandlebarsTemplateEngine());


        //čitanje i upisivanje u JSON kada se menja proizvod
        post("/izmeniProizvod", (request, response) -> {
            String nazivProizvoda = request.queryParams("ime");
            Double cenaProizvoda = Double.parseDouble(request.queryParams("cena"));
            String opisProizvoda = request.queryParams("opis");
            String slikaProizvoda = request.queryParams("slika");
            int idProizvoda = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            for (Proizvod p : lista ) {
                if (p.getId() == idProizvoda) {
                    p.setIme(nazivProizvoda);
                    p.setCena(cenaProizvoda);
                    p.setOpis(opisProizvoda);
                    p.setSlika(slikaProizvoda);
                    polja.put("poruka","Uspesno izmenjen proizvod!");
                    break;
                }else {
                    polja.put("poruka", "Niste uspeli da izmenite proizvod!");
                }

            }
            Data.writeToJSON(lista, path);
            return new ModelAndView(polja,"izmena.hbs");
        }, new HandlebarsTemplateEngine() );



        //brisanje proizvoda
        get("/obrisi",(request, response) -> {
            response.type("text/text");
            int id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            String poruka = "";
            for (Proizvod p : lista) {
                if(p.getId() == id) {
                    lista.remove(p);
                    Data.writeToJSON(lista,path);
                    poruka = "Uspesno obrisan proizvod!";
                    break;
                } else {
                    poruka = "Proizvod nije obrisan!";
                }
            }
            System.out.println(lista.size());

            return poruka;
        });


        //Prikaz šablona stranice za dodavanje kategorije
        get("/dodajKategoriju",(request, response) -> {
            if(request.session().attribute("user")==null){
                response.redirect("/");
                return null; //zastita sesije
            }
            return new ModelAndView(null,"dodajKategoriju.hbs");
        },new HandlebarsTemplateEngine());

        //metoda post i dodaj kategoriju

        post("/dodajKat",(request, response) -> {
            String naziv = request.queryParams("naziv").toUpperCase();
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            Kategorija k = new Kategorija(kategorije.size()+1,naziv);
            for (int i = 0;i<kategorije.size();i++) {
                if(kategorije.get(i).getNaziv().equals(naziv)) {
                    break;
                } else{
                    if(i == kategorije.size()-1){
                        kategorije.add(k);
                        polja.put("poruka","Uspešno dodata kategorija");
                    }
                }
            }
            Data2.writeToJSON(kategorije,"kategorije.json");
            return new ModelAndView(polja,"dodajKategoriju.hbs");
        },new HandlebarsTemplateEngine());

        //obraditi zahtev za brisanje kategorije koja nije dodeljena, ako je dodeljena ne moze se obrisati
        get("/izbrisiKategoriju",(request, response) -> {
            String kat = request.queryParams("kategorija").toUpperCase();
            String poruka = "";
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            Boolean provera = false;
            for (Proizvod p : Data.readFromJson(path)) {
                String kategorijaProizvoda = p.getIme().split(" ")[0].toUpperCase();
                if(kategorijaProizvoda.contains(kat)) {
                    poruka = "Ne mozete obrisati kategoriju koja ima proizvode";
                    provera = true;
                    break;
                }
            }
            if(!provera) {
                for (Kategorija k : kategorije) {
                    if (kat.equals(k.getNaziv())) {
                        kategorije.remove(k);
                        poruka = "Uspesno obrisana kategorija!";
                        Data2.writeToJSON(kategorije, "kategorije.json");
                        break;
                    }
                }
            }
            return poruka;
        });



        //obradi zahtev za promenu vidljivosti proizvoda na admin panelu i upisati u JSON
        post("/promeniVidljivost", (request, response) -> {
            boolean vidljivost = Boolean.parseBoolean(request.queryParams("vidljivost"));
            Integer id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            for (Proizvod p: lista) {
                if  (p.getId() == id) {
                    p.setVidljivost(vidljivost);
                    break;
                }
            }

            Data.writeToJSON(lista, path);
            return null;
        });


        //prikazivanje korpe
        ArrayList<Proizvod> lista = new ArrayList<>(); //prazna lista

        get("/korpa",(request, response) -> {
            polja.put("proizvodi",lista);
            return new ModelAndView(polja,"korpa.hbs");
        }, new HandlebarsTemplateEngine());


        // preko post rute, obradi zahtev i u korpu ubaci proizvode koji su poruceni
        post("/staviKorpu",(request, response) -> {
            int id = Integer.parseInt(request.queryParams("id"));
            for (Proizvod p: Data.readFromJson(path)) {
                if (p.getId() == id) {
                    lista.add(p);
                    break;
                }
            }
            return null;

        });


    }
}
